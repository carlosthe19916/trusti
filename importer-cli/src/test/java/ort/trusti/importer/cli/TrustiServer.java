package ort.trusti.importer.cli;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class TrustiServer implements QuarkusTestResourceLifecycleManager {

    private WireMockServer server;

    @Override
    public Map<String, String> start() {
        server = new WireMockServer(
                wireMockConfig().port(8080)
        );
        server.start();

        server.stubFor(post(urlEqualTo("/advisories"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                )
        );
        server.stubFor(put(urlEqualTo("/advisories"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                )
        );

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
