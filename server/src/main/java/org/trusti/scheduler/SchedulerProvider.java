package org.trusti.scheduler;

import org.trusti.models.jpa.entity.TaskEntity;

public interface SchedulerProvider {

    void createTask(TaskEntity taskEntity);

    void cancelTask(TaskEntity taskEntity);

}
