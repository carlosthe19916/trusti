package ort.trusti.importer.cli;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.trusti.importer.ImporterCamelHeaders;
import org.trusti.importer.ImporterRoute;

@ApplicationScoped
public class RemoteImporterRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(ImporterRoute.REGISTER_ACTIVITY_ROUTE_ID)
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .toD("${header." + ImporterCamelHeaders.REMOTE_IMPORTER_TRUSTI_SERVER_URL + "}/tasks/${header." + ImporterCamelHeaders.IMPORTER_TASK_ID + "}")
                .setBody(header(Exchange.HTTP_RESPONSE_CODE));

        from(ImporterRoute.IMPORT_FILE_ROUTE_ID)
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .toD("${header." + ImporterCamelHeaders.REMOTE_IMPORTER_TRUSTI_SERVER_URL + "}/tasks/${header." + ImporterCamelHeaders.IMPORTER_TASK_ID + "}/advisories")
                .setBody(header(Exchange.HTTP_RESPONSE_CODE));
    }

}
