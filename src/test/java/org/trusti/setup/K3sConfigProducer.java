package org.trusti.setup;

import io.fabric8.kubernetes.client.Config;
import io.quarkus.kubernetes.client.runtime.KubernetesClientBuildConfig;
import io.quarkus.kubernetes.client.runtime.KubernetesConfigProducer;
import io.quarkus.runtime.TlsConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Alternative
@Priority(1)
@Singleton
public class K3sConfigProducer extends KubernetesConfigProducer {
    //Injects the kubeConfigYaml that you've set in the K3sResource
    @ConfigProperty(name = "kubeConfigYaml")
    String kubeConfigYaml;

    //Returns the kubeConfigYaml as the config
    @Singleton
    @Produces
    public Config config(KubernetesClientBuildConfig buildConfig, TlsConfig tlsConfig) {
        return Config.fromKubeconfig(kubeConfigYaml);
    }
}
