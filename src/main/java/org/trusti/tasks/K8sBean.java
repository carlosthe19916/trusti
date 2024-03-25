package org.trusti.tasks;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.apache.camel.Header;
import org.trusti.dto.TaskDto;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.TaskEntity;

@ApplicationScoped
@Named("K8sBean")
@RegisterForReflection
public class K8sBean {

    @Transactional
    public void updateTask(@Header("task") TaskDto taskDto) {
        TaskEntity taskEntity = TaskEntity.findById(taskDto.id());
        taskEntity.state = TaskState.Created;
        taskEntity.persist();
    }

}
