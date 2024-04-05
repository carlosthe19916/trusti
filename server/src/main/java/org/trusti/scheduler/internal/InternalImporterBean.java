package org.trusti.scheduler.internal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.trusti.dto.TaskDto;
import org.trusti.importer.ImporterCamelHeaders;
import org.trusti.importer.ImporterTaskDto;
import org.trusti.models.jpa.TaskRepository;
import org.trusti.models.jpa.entity.TaskEntity;

@ApplicationScoped
@Named("InternalImporterBean")
@RegisterForReflection
public class InternalImporterBean {

    @Inject
    TaskRepository taskRepository;

    @Transactional
    public void updateTask(
            @Body ImporterTaskDto importerTaskDto,
            @Header(ImporterCamelHeaders.IMPORTER_TASK_ID) Long taskId
    ) {
        TaskEntity taskEntity = TaskEntity.findById(taskId);
        TaskDto taskDto = TaskDto.from(importerTaskDto);
        taskRepository.updateFrom(taskEntity, taskDto);
        taskEntity.persist();
    }

}
