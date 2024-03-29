package org.trusti.tasks;

import io.fabric8.kubernetes.api.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kubernetes.KubernetesConstants;
import org.apache.camel.component.kubernetes.KubernetesOperations;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.trusti.dto.TaskDto;
import org.trusti.models.SourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class K8sRoute extends RouteBuilder {

    private static final String TOKEN = "token";

    @ConfigProperty(name = "trusti.namespace")
    String namespace;

    @ConfigProperty(name = "trusti.domain")
    String trustiDomain;

    @ConfigProperty(name = "trusti.importer.image")
    String importerImage;

    @Override
    public void configure() throws Exception {
        from("direct:create-task")
            .process(exchange -> {
                    TaskDto taskDto = exchange.getIn().getBody(TaskDto.class);
                    exchange.getIn().setHeader("task", taskDto);
                })

                // Create secrets
            .process(exchange -> {
                    TaskDto taskDto = exchange.getIn().getHeader("task", TaskDto.class);
                    exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_NAMESPACE_NAME, namespace);
                    exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_SECRET, generateSecret(taskDto));
                })
            .toF("kubernetes-secrets:///?operation=" + KubernetesOperations.CREATE_SECRET_OPERATION)

                // Create pod
            .process(exchange -> {
                    TaskDto taskDto = exchange.getIn().getHeader("task", TaskDto.class);
                    exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_NAMESPACE_NAME, namespace);
                    exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_POD_SPEC, generatePodSpec(taskDto));
                    exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_POD_NAME, taskDto.name());
                })
            .toF("kubernetes-pods:///?operation=" + KubernetesOperations.CREATE_POD_OPERATION)
            .bean("K8sBean", "updateTask");
    }

    private Map<String, String> generateLabels(TaskDto taskDto) {
        return Map.of(
                "app", "trusti",
                "role", "task",
                "task", taskDto.id().toString()
        );
    }

    private Secret generateSecret(TaskDto taskDto) {
        return new SecretBuilder()
                .withNewMetadata()
                .withName(taskDto.name())
                .withLabels(generateLabels(taskDto))
                .endMetadata()
                .addToStringData(TOKEN, "123")
                .build();
    }

    private PodSpec generatePodSpec(TaskDto taskDto) {
        List<String> args = new ArrayList<>();
        List<EnvVar> envVars = new ArrayList<>();

        // Args
        if (taskDto.source().type().equals(SourceType.git)) {
            args.add("git");
        } else {
            args.add("http");
        }

        args.add(taskDto.source().url());

        // Git ENVs
        if (taskDto.source().gitDetails() != null) {
            envVars.add(new EnvVarBuilder().withName("WORKSPACE")
                    .withValue(".")
                    .build()
            );
            envVars.add(new EnvVarBuilder().withName("GIT_REF")
                    .withValue(taskDto.source().gitDetails().ref())
                    .build()
            );
            envVars.add(new EnvVarBuilder().withName("GIT_WORKING_DIRECTORY")
                    .withValue(taskDto.source().gitDetails().workingDirectory())
                    .build()
            );
        }

        // TARGET_URL
        envVars.add(new EnvVarBuilder().withName("TARGET_URL")
                .withValue(trustiDomain + "/tasks/" + taskDto.id() + "/advisories")
                .build()
        );

        // Tokens
        envVars.add(new EnvVarBuilder().withName("TRUSTI_TOKEN")
                .withNewValueFrom()
                .withNewSecretKeyRef()
                .withName(taskDto.name())
                .withKey(TOKEN)
                .withOptional(false)
                .endSecretKeyRef()
                .endValueFrom()
                .build()
        );

        return new PodSpecBuilder()
                .withRestartPolicy("Always")
                .withContainers(new ContainerBuilder()
                        .withName("task")
                        .withImage(importerImage)
                        .withImagePullPolicy("Always")
                        .withEnv(envVars)
                        .withArgs(args)
                        .withResources(new ResourceRequirementsBuilder()
                                .withRequests(Map.of(
                                        "cpu", new Quantity("0.5"),
                                        "memory", new Quantity("0.5Gi")
                                ))
                                .withLimits(Map.of(
                                        "cpu", new Quantity("1"),
                                        "memory", new Quantity("1Gi")
                                ))
                                .build()
                        )
                        .withVolumeMounts(new VolumeMountBuilder()
                                .withName("workspace-pvol")
                                .withMountPath("/opt/trusti/workspace")
                                .build()
                        )
                        .build()
                )
                .withVolumes(new VolumeBuilder()
                        .withName("workspace-pvol")
                        .withNewEmptyDir()
                        .endEmptyDir()
                        .build()
                )
                .build();
    }

}
