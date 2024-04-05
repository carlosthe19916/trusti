package org.trusti.scheduler.internal;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.trusti.importer.ImporterCamelHeaders;
import org.trusti.importer.ImporterRoute;

@ApplicationScoped
public class InternalImporterRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(ImporterRoute.REGISTER_ACTIVITY_ROUTE_ID)
                .bean("InternalImporterBean", "updateTask");

        from(ImporterRoute.IMPORT_FILE_ROUTE_ID)
                .to("direct:import-advisory");
    }

}
