package org.trusti.resources;

import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.quarkus.test.junit.callback.QuarkusTestBeforeClassCallback;
import org.eclipse.microprofile.config.ConfigProvider;

public class MyQuarkusTestBeforeEachCallback implements QuarkusTestBeforeClassCallback {

    @Override
    public void beforeClass(Class<?> testClass) {
        var config = ConfigProvider.getConfig();

        var apiServerUrl = config.getValue("quarkus.kubernetes-client.api-server-url", String.class);
        var token = config.getOptionalValue("quarkus.kubernetes-client.token", String.class);
        var namespace = config.getValue("quarkus.kubernetes-client.namespace", String.class);
        var caCertData = config.getOptionalValue("quarkus.kubernetes-client.ca-cert-data", String.class);
        var clientCertData = config.getOptionalValue("quarkus.kubernetes-client.client-cert-data", String.class);
        var clientKeyData = config.getOptionalValue("quarkus.kubernetes-client.client-key-data", String.class);
        var clientKeyAlgo = config.getOptionalValue("quarkus.kubernetes-client.client-key-algo", String.class);

        KubernetesClientBuilder kubernetesClientBuilder = new KubernetesClientBuilder()
                .withConfig(new ConfigBuilder()
                        .withMasterUrl(apiServerUrl)
                        .withOauthToken(token.orElse(null))
                        .withNamespace(namespace)
                        .withCaCertData(caCertData.orElse(null))
                        .withClientCertData(clientCertData.orElse(null))
                        .withClientKeyData(clientKeyData.orElse(null))
                        .withClientKeyAlgo(clientKeyAlgo.orElse(null))
                        .build()
                );

        try (KubernetesClient kubernetesClient = kubernetesClientBuilder.build()) {
            if (kubernetesClient.serviceAccounts().inNamespace(namespace).withName("trusti").get() == null) {
                kubernetesClient.serviceAccounts().inNamespace(namespace).resource(new ServiceAccountBuilder()
                                .withNewMetadata()
                                .withName("trusti")
                                .endMetadata()
                                .build()
                        )
                        .create();
            }
        }
    }
}
