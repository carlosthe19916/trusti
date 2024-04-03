package org.trusti.tasks;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    @ConfigProperty(name = "quarkus.kubernetes-client.namespace", defaultValue = "default")
    String namespace;

    @ConfigProperty(name = "trusti.domain")
    String trustiDomain;

    @ConfigProperty(name = "trusti.importer.image")
    String importerImage;

    @ConfigProperty(name = "trusti.importer.workspace", defaultValue = "/tmp/workspace")
    String importerWorkspace;

    @ConfigProperty(name = "trusti.importer.resources.requests.memory")
    Optional<String> importerResourcesRequestsMemory;

    @ConfigProperty(name = "trusti.importer.resources.requests.cpu")
    Optional<String> importerResourcesRequestsCpu;

    @ConfigProperty(name = "trusti.importer.resources.limits.memory")
    Optional<String> importerResourcesLimitsMemory;

    @ConfigProperty(name = "trusti.importer.resources.limits.cpu")
    Optional<String> importerResourcesLimitsCpu;

    public void onCreatedEvent(@Observes @State(value = TaskState.Created) TaskEntity taskEntity) {
        kubernetesClient.secrets()
                .inNamespace(namespace)
                .resource(generateSecret(taskEntity))
                .create();

        Job pod = generateJob(taskEntity);

        Job job = kubernetesClient.batch().v1().jobs()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        QuarkusTransaction.begin();

        taskEntity = TaskEntity.findById(taskEntity.id);
        taskEntity.state = TaskState.Ready;
        taskEntity.job = job.getMetadata().getName();
        taskEntity.image = job.getSpec().getTemplate().getSpec().getContainers().stream()
                .map(Container::getImage)
                .collect(Collectors.joining(","));
        taskEntity.persist();

        QuarkusTransaction.commit();
    }

    public void onCanceledEvent(@Observes @State(value = TaskState.Canceled) TaskEntity taskEntity) {
        ScalableResource<Job> jobResource = kubernetesClient.batch().v1().jobs()
                .inNamespace(namespace)
                .withName(taskEntity.job);
        if (jobResource.get() != null) {
            jobResource.delete();
        }
    }

    private Map<String, String> generateLabels(TaskEntity taskEntity) {
        return Map.of(
                "app", "trusti",
                "role", "task",
                "task", taskEntity.id.toString()
        );
    }

    private Secret generateSecret(TaskEntity taskEntity) {
        return new SecretBuilder()
                .withNewMetadata()
                .withName(taskEntity.name)
                .withLabels(generateLabels(taskEntity))
                .endMetadata()
                .addToStringData(TOKEN, "123")
                .build();
    }

    private Job generateJob(TaskEntity taskEntity) {
        List<String> args = new ArrayList<>();
        List<EnvVar> envVars = new ArrayList<>();

        // Args
        if (taskEntity.source.type.equals(SourceType.git)) {
            args.add("git");
        } else {
            args.add("http");
        }

        args.add(taskEntity.source.url);

        // Git ENVs
        if (taskEntity.source.gitDetails != null) {
            envVars.add(new EnvVarBuilder().withName("WORKSPACE")
                    .withValue(importerWorkspace)
                    .build()
            );
            envVars.add(new EnvVarBuilder().withName("GIT_REF")
                    .withValue(taskEntity.source.gitDetails.ref)
                    .build()
            );
            envVars.add(new EnvVarBuilder().withName("GIT_WORKING_DIRECTORY")
                    .withValue(taskEntity.source.gitDetails.workingDirectory)
                    .build()
            );
        }

        // TARGET_URL
        envVars.add(new EnvVarBuilder().withName("TARGET_URL")
                .withValue(trustiDomain + "/tasks/" + taskEntity.id)
                .build()
        );

        // Tokens
        envVars.add(new EnvVarBuilder().withName("TRUSTI_TOKEN")
                .withNewValueFrom()
                .withNewSecretKeyRef()
                .withName(taskEntity.name)
                .withKey(TOKEN)
                .withOptional(false)
                .endSecretKeyRef()
                .endValueFrom()
                .build()
        );

        return new JobBuilder()
                .withNewMetadata()
                .withName(taskEntity.name)
                .withLabels(generateLabels(taskEntity))
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
