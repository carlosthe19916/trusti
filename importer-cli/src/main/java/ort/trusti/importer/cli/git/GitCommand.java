package ort.trusti.importer.cli.git;

import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.trusti.importer.ImporterCamelHeaders;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;

@Command(name = "git", mixinStandardHelpOptions = true, description = "Import data from a git repository")
public class GitCommand implements Runnable {

    @Inject
    ProducerTemplate producerTemplate;

    @Parameters(paramLabel = "repository", description = "The git repository.")
    String gitRepository;

    @CommandLine.Option(names = {"--task-id", "-ti"}, required = true, defaultValue = "${env:TASK_ID}", description = "The Task ID to which this process belongs to.")
    Long taskId;

    @CommandLine.Option(names = {"--trusti-server-url", "-tsu"}, required = true, defaultValue = "${env:TRUSTI_SERVER_URL}", description = "The URL of Trusti running server.")
    String trustiServerUrl;

    @CommandLine.Option(names = {"--ref", "-r"}, defaultValue = "${env:GIT_REF}", description = "The branch, tag or SHA to checkout")
    String gitRef;

    @CommandLine.Option(names = {"--workspace", "-ws"}, defaultValue = "${env:WORKSPACE:-target/repository}", description = "Where the repository will be clone")
    String workspace;

    @CommandLine.Option(names = {"--working-directory", "-wd"}, defaultValue = "${env:GIT_WORKING_DIRECTORY}", description = "Directory within the repository.")
    String gitWorkingDirectory;

    @Override
    public void run() {
        System.out.println("Started ingestion from " + gitRepository);

        Map<String, Object> headers = ImporterCamelHeaders.git(
                taskId,
                workspace,
                gitRepository,
                gitRef,
                gitWorkingDirectory
        );
        headers.put(ImporterCamelHeaders.REMOTE_IMPORTER_TRUSTI_SERVER_URL, trustiServerUrl);

        producerTemplate.requestBodyAndHeaders("direct:start-importer", null, headers);

        System.out.println("Finished successfully");
    }

}
