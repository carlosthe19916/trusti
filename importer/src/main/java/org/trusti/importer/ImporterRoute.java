package org.trusti.importer;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import java.util.Date;

@ApplicationScoped
public class ImporterRoute extends EndpointRouteBuilder {

    public final static String REGISTER_ACTIVITY_ROUTE_ID = "direct:register-task-activity";
    public final static String IMPORT_FILE_ROUTE_ID = "direct:import-file";

    @Override
    public void configure() throws Exception {
        from("direct:start-importer")
                .onException(Throwable.class)
                    .process(exchange -> {
                        Throwable error = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

                        String errorMessage = error.getMessage().substring(0, Math.min(error.getMessage().length(), 10));
                        exchange.getIn().setBody(new ImporterTaskDto(ImporterTaskDto.TaskState.Failed, null, new Date(), errorMessage));
                    })
                    .to(REGISTER_ACTIVITY_ROUTE_ID)
                .end()

                .setBody(constant(new ImporterTaskDto(ImporterTaskDto.TaskState.Running, new Date(), null, null)))
                .to(REGISTER_ACTIVITY_ROUTE_ID)

                .toD("direct:start-importer-${header." + ImporterCamelHeaders.IMPORTER_TYPE_HEADER + "}")

                .setBody(constant(new ImporterTaskDto(ImporterTaskDto.TaskState.Succeeded, null, new Date(), null)))
                .to(REGISTER_ACTIVITY_ROUTE_ID);
    }

}
