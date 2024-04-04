package org.trusti.git;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.trusti.TrustiServer;

@QuarkusTestResource(TrustiServer.class)
@QuarkusMainTest
class GitCommandTest {

    @Test
    @Launch(value = {
            "git",
            "--target-url=http://localhost:8080/advisories",
            "--working-directory=src/test/resources",
            "https://github.com/carlosthe19916/trusti.git"
    })
    public void testLaunchCommand(LaunchResult result) {
        Assertions.assertEquals(0, result.exitCode());
    }

}