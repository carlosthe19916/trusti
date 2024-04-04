package org.trusti.http;

import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.trusti.Constants;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;

@Command(name = "http", mixinStandardHelpOptions = true, description = "Import data from a HTTP server")
public class HttpCommand implements Runnable {

    @Inject
    ProducerTemplate producerTemplate;

    @Parameters(paramLabel = "serverUrl", description = "The HTTP server where the CSAF files are stored.")
    String serverUrl;

    @CommandLine.Option(names = {"--target-url", "-tu"}, required = true, defaultValue = "${env:TARGET_URL}", description = "The URL where the files are going to be sent to.")
    String targetUrl;

    @Override
    public void run() {
        System.out.printf("Started ingestion from %s", serverUrl);

        Map<String, Object> headers = Map.of(
                Constants.IMPORTER_TYPE_HEADER, "http",
                Constants.IMPORTER_OUTPUT_BASE_URL, targetUrl,
                Constants.IMPORTER_HTTP_SERVER_URL, serverUrl
        );

        producerTemplate.requestBodyAndHeaders("direct:start-importer", null, headers);

        System.out.println("Finished successfully");
    }

}
