package ort.trusti.importer.cli.http;

import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.trusti.importer.ImporterCamelHeaders;
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

    @CommandLine.Option(names = {"--task-id", "-ti"}, required = true, defaultValue = "${env:TASK_ID}", description = "The Task ID to which this process belongs to.")
    Long taskId;

    @CommandLine.Option(names = {"--trusti-server-url", "-tsu"}, required = true, defaultValue = "${env:TRUSTI_SERVER_URL}", description = "The URL of Trusti running server.")
    String trustiServerUrl;

    @Override
    public void run() {
        System.out.printf("Started ingestion from %s", serverUrl);

        Map<String, Object> headers = ImporterCamelHeaders.http(taskId, serverUrl);
        headers.put(ImporterCamelHeaders.REMOTE_IMPORTER_TRUSTI_SERVER_URL, trustiServerUrl);

        producerTemplate.requestBodyAndHeaders("direct:start-importer", null, headers);

        System.out.println("Finished successfully");
    }

}
