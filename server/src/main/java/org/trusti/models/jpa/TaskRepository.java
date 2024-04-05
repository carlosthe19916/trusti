package org.trusti.models.jpa;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.trusti.dto.TaskDto;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.TaskEntity;
import org.trusti.scheduler.Scheduler;

@Transactional
@ApplicationScoped
public class TaskRepository implements PanacheRepository<TaskEntity> {

    @Inject
    Scheduler scheduler;

    public void updateFrom(TaskEntity taskEntity, TaskDto taskDto) {
        if (taskDto.state() != null) {
            taskEntity.state = taskDto.state();

            if (taskEntity.state == TaskState.Canceled) {
                scheduler.getInstance().cancelTask(taskEntity);
            }
        }
        if (taskDto.started() != null) {
            taskEntity.started = taskDto.started();
        }
        if (taskDto.terminated() != null) {
            taskEntity.terminated = taskDto.terminated();
        }
        if (taskDto.error() != null) {
            taskEntity.error = taskDto.error();
        }
    }

}
