package org.trusti.ui;

import io.quarkus.qute.EngineBuilder;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.event.Observes;

public class StaticResources {

    void installRoute(@Observes StartupEvent startupEvent, Router router) {
        router.route()
                .path("/*")
                .handler(StaticHandler
                        .create("META-INF/resources/client/dist")
                );
    }

    void configureEngine(@Observes EngineBuilder builder) {
        builder.addParserHook(parserHelper -> {
            parserHelper.addContentFilter(contents -> contents.replace("??", "or"));
            parserHelper.addContentFilter(contents -> contents.replace("<%= ", "{"));
            parserHelper.addContentFilter(contents -> contents.replace(" %>", "}"));
        });
    }
}
