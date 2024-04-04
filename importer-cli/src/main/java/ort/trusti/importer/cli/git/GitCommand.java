package ort.trusti.importer.cli.git;

import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.trusti.importer.Constants;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.HashMap;
import java.util.Map;

@Command(name = "git", mixinStandardHelpOptions = true, description = "Import data from a git repository")
public class GitCommand implements Runnable {

    @Inject
    ProducerTemplate producerTemplate;

    @Parameters(paramLabel = "repository", description = "The git repository.")
    String gitRepository;

    @CommandLine.Option(names = {"--target-url", "-tu"}, required = true, defaultValue = "${env:TARGET_URL}", description = "The URL where the files are going to be sent to.")
    String targetUrl;

    @CommandLine.Option(names = {"--ref", "-r"}, defaultValue = "${env:GIT_REF}", description = "The branch, tag or SHA to checkout")
    String gitRef;

    @CommandLine.Option(names = {"--workspace", "-ws"}, defaultValue = "${env:WORKSPACE:-target/repository}", description = "Where the repository will be clone")
    String workspace;

    @CommandLine.Option(names = {"--working-directory", "-wd"}, defaultValue = "${env:GIT_WORKING_DIRECTORY}", description = "Directory within the repository.")
    String gitWorkingDirectory;

    @Override
    public void run() {
        System.out.println("Started ingestion from " + gitRepository);

        Map<String, Object> headers = new HashMap<>();
        headers.put(Constants.IMPORTER_TYPE_HEADER, "git");
        headers.put(Constants.IMPORTER_OUTPUT_BASE_URL, targetUrl);

        headers.put(Constants.IMPORTER_GIT_WORKSPACE, workspace);
        headers.put(Constants.IMPORTER_GIT_REPOSITORY, gitRepository);
        headers.put(Constants.IMPORTER_GIT_REF, gitRef);
        headers.put(Constants.IMPORTER_GIT_WORKING_DIRECTORY, gitWorkingDirectory);

        producerTemplate.requestBodyAndHeaders("direct:start-importer", null, headers);

        System.out.println("Finished successfully");
    }

}
