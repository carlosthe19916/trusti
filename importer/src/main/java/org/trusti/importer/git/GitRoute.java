package org.trusti.importer.git;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.trusti.importer.ImporterCamelHeaders;
import org.trusti.importer.ImporterRoute;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class GitRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:start-importer-git")
                .setBody(header(ImporterCamelHeaders.IMPORTER_GIT_REPOSITORY))
                .to("direct:clone-git")

                .setBody(header(ImporterCamelHeaders.IMPORTER_GIT_WORKSPACE))
                .to("direct:import-git");

        from("direct:clone-git")
                .process(exchange -> {
                    String workspace = exchange.getIn().getHeader(ImporterCamelHeaders.IMPORTER_GIT_WORKSPACE, String.class);
                    Path workspacePath = Paths.get(workspace);
                    if (workspacePath.toFile().exists()) {
                        try (var stream = Files.walk(workspacePath)) {
                            stream
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(File::delete);
                        }
                    }
                })

                .toD("git:${header." + ImporterCamelHeaders.IMPORTER_GIT_WORKSPACE + "}?operation=clone&remotePath=${header." + ImporterCamelHeaders.IMPORTER_GIT_REPOSITORY + "}&branchName=${header." + ImporterCamelHeaders.IMPORTER_GIT_REF + "}");

        from("direct:import-git")
                .process(exchange -> {
                    String rootDirectory = exchange.getIn().getBody(String.class);
                    String workingDirectory = exchange.getIn().getHeader(ImporterCamelHeaders.IMPORTER_GIT_WORKING_DIRECTORY, String.class);

                    Path path = Paths.get(rootDirectory, workingDirectory != null ? workingDirectory : "");
                    try (var stream = Files.walk(path)) {
                        List<File> filesList = stream
                                .filter(Files::isRegularFile)
                                .filter(e -> e.getFileName().toString().endsWith(".json"))
                                .map(e -> e.toFile())
                                .toList();

                        exchange.getIn().setBody(filesList);
                    }
                })
                .split(body()).parallelProcessing()
                    .to(ImporterRoute.IMPORT_FILE_ROUTE_ID)
                .end();
    }

}
