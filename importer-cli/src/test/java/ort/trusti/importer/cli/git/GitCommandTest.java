package ort.trusti.importer.cli.git;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ort.trusti.importer.cli.TrustiServer;

@QuarkusTestResource(TrustiServer.class)
@QuarkusMainTest
class GitCommandTest {

    @Test
    @Launch(value = {
            "git",
            "--target-url=http://localhost:8080/advisories",
            "--working-directory=server/src/test/resources",
            "https://github.com/carlosthe19916/trusti.git"
    })
    public void testLaunchCommand(LaunchResult result) {
        Assertions.assertEquals(0, result.exitCode());
    }

}