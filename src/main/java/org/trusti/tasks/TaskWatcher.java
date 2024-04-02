package org.trusti.tasks;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.trusti.dto.TaskDto;
import org.trusti.models.SourceType;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskWatcher {

    private static final String TOKEN = "token";

    @Inject
    KubernetesClient kubernetesClient;

    @ConfigProperty(name = "quarkus.kubernetes-client.namespace")
    Optional<String> namespace;

    @ConfigProperty(name = "trusti.domain")
    String trustiDomain;

    @ConfigProperty(name = "trusti.importer.image")
    String importerImage;

    @ConfigProperty(name = "trusti.importer.resources.requests.memory")
    Optional<String> importerResourcesRequestsMemory;

    @ConfigProperty(name = "trusti.importer.resources.requests.cpu")
    Optional<String> importerResourcesRequestsCpu;

    @ConfigProperty(name = "trusti.importer.resources.limits.memory")
    Optional<String> importerResourcesLimitsMemory;

    @ConfigProperty(name = "trusti.importer.resources.limits.cpu")
    Optional<String> importerResourcesLimitsCpu;

    public void onEvent(@Observes TaskDto taskDto) {
        kubernetesClient.secrets()
                .inNamespace(namespace.orElse("default"))
                .resource(generateSecret(taskDto))
                .create();

        Job pod = generateJob(taskDto);

        Job job = kubernetesClient.batch().v1().jobs()
                .inNamespace(namespace.orElse("default"))
                .resource(pod)
                .create();

        QuarkusTransaction.begin();

        TaskEntity taskEntity = TaskEntity.findById(taskDto.id());
        taskEntity.state = TaskState.Created;
        taskEntity.job = job.getMetadata().getName();
        taskEntity.image = job.getSpec().getTemplate().getSpec().getContainers().stream()
                .map(Container::getImage)
                .collect(Collectors.joining(","));
        taskEntity.persist();

        QuarkusTransaction.commit();
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

    private Job generateJob(TaskDto taskDto) {
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

        return new JobBuilder()
                .withNewMetadata()
                .withName(taskDto.name())
                .withLabels(generateLabels(taskDto))
                .endMetadata()
                .withSpec(new JobSpecBuilder()
                        .withBackoffLimit(0)
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withSpec(new PodSpecBuilder()
                                        .withRestartPolicy("Never")
                                        .withContainers(new ContainerBuilder()
                                                .withName("importer")
                                                .withImage(importerImage)
                                                .withImagePullPolicy("Always")
                                                .withEnv(envVars)
                                                .withArgs(args)
                                                .withResources(new ResourceRequirementsBuilder()
                                                        .withRequests(Map.of(
                                                                "cpu", new Quantity(importerResourcesRequestsCpu.orElse("0.5")),
                                                                "memory", new Quantity(importerResourcesRequestsMemory.orElse("0.5Gi"))
                                                        ))
                                                        .withLimits(Map.of(
                                                                "cpu", new Quantity(importerResourcesLimitsCpu.orElse("1")),
                                                                "memory", new Quantity(importerResourcesLimitsMemory.orElse("1Gi"))
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
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();
    }
}
