package org.trusti.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jsonvalidator.JsonValidationException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.mapper.AdvisoryMapper;
import org.trusti.models.jpa.entity.AdvisoryEntity;
import schemas.csaf.Csaf;
import schemas.osv.Osv;

@ApplicationScoped
public class AdvisoryImportResource extends RouteBuilder {

    @Inject
    AdvisoryMapper advisoryMapper;

    @Override
    public void configure() throws Exception {
        from("rest:post:advisories?consumes=application/json&produces=application/json")
                .multicast((oldExchange, newExchange) -> {
                    if (newExchange.getIn().getBody() != null) {
                        return newExchange;
                    } else {
                        return oldExchange;
                    }
                })
                    .to("direct:csaf")
                    .to("direct:osv")
                .end()
                .choice()
                    .when(body().isNotNull())
                        .process(exchange -> {
                            AdvisoryEntity entity = exchange.getIn().getBody(AdvisoryEntity.class);
                            exchange.getIn().setBody(advisoryMapper.toDto(entity));
                        })
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(RestResponse.StatusCode.CREATED))
                        .marshal().json(JsonLibrary.Jackson)
                    .endChoice()
                    .otherwise()
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(RestResponse.StatusCode.BAD_REQUEST))
                    .endChoice()
                .end();

        from("direct:csaf")
                .onException(JsonValidationException.class)
                    .handled(true)
                    .logHandled(false)
                    .setBody().simple("${null}")
                .end()
                .to("json-validator:schema/csaf/csaf.json")
                .unmarshal().json(JsonLibrary.Jackson, Csaf.class)
                .bean("AdvisoryImporterBean", "csaf");

        from("direct:osv")
                .onException(JsonValidationException.class)
                    .handled(true)
                    .logHandled(false)
                    .setBody().simple("${null}")
                .end()
                .to("json-validator:schema/osv/osv.json")
                .unmarshal().json(JsonLibrary.Jackson, Osv.class)
                .bean("AdvisoryImporterBean", "osv");
    }
}
