package org.trusti.ui;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UiEnv(
        String NODE_ENV,
        String VERSION,
        String MOCK,

        String AUTH_REQUIRED,
        String OIDC_SERVER_URL,
        String OIDC_CLIENT_ID,
        String OIDC_SCOPE,

        String UI_INGRESS_PROXY_BODY_SIZE,

        String ANALYTICS_ENABLED,
        String ANALYTICS_WRITE_KEY
) {
}
