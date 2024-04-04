package org.trusti;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.Date;

@ApplicationScoped
public class SendFilesRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:start-importer")
                .onException(Throwable.class)
                    .process(exchange -> {
                        Throwable error = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

                        String errorMessage = error.getMessage().substring(0, Math.min(error.getMessage().length(), 10));
                        exchange.getIn().setBody(new TaskDto(TaskDto.TaskState.Failed, null, new Date(), errorMessage));
                    })
                    .to("direct:register-task-activity")
                .end()

                .setBody(constant(new TaskDto(TaskDto.TaskState.Running, new Date(), null, null)))
                .to("direct:register-task-activity")

                .toD("direct:start-importer-${header." + Constants.IMPORTER_TYPE_HEADER + "}")

                .setBody(constant(new TaskDto(TaskDto.TaskState.Succeeded, null, new Date(), null)))
                .to("direct:register-task-activity");

        from("direct:register-task-activity")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .toD("${header." + Constants.IMPORTER_OUTPUT_BASE_URL + "}")
                .setBody(header(Exchange.HTTP_RESPONSE_CODE));

        from("direct:send-file")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .toD("${header." + Constants.IMPORTER_OUTPUT_BASE_URL + "}")
                .setBody(header(Exchange.HTTP_RESPONSE_CODE));
    }

}
