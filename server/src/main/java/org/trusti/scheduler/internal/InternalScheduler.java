package org.trusti.scheduler.internal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.quartz.*;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.TaskEntity;
import org.trusti.scheduler.SchedulerProvider;
import org.trusti.scheduler.SchedulerProviderType;

import java.util.UUID;

@ApplicationScoped
@SchedulerProviderType(SchedulerProviderType.Type.INTERNAL)
public class InternalScheduler implements SchedulerProvider {

    public static final String TRIGGER_ID_NAME = "id";

    @Inject
    Scheduler quartz;

    @RegisterForReflection
    public static class ImporterJob implements Job {

        @Inject
        InternalTaskExecutor internalTaskExecutor;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Long taskId = Long.valueOf((String) context.getTrigger().getJobDataMap().get(InternalScheduler.TRIGGER_ID_NAME));
            internalTaskExecutor.runTask(taskId);
        }
    }

    private JobKey jobKeyFrom(TaskEntity taskEntity) {
        return JobKey.jobKey(taskEntity.source.id.toString(), "import-files");
    }

    @Transactional
    @Override
    public void createTask(TaskEntity taskEntity) {
        try {
            // Schedule
            JobKey jobKey = jobKeyFrom(taskEntity);
            JobDetail jobDetail = JobBuilder
                    .newJob(ImporterJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            if (!quartz.checkExists(jobDetail.getKey())) {
                quartz.addJob(jobDetail, false);
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobKey)
                    .withIdentity(UUID.randomUUID().toString())
                    .usingJobData(TRIGGER_ID_NAME, taskEntity.id.toString())
                    .startNow()
                    .build();

            quartz.scheduleJob(trigger);

            // Update
            taskEntity = TaskEntity.findById(taskEntity.id);
            taskEntity.state = TaskState.Ready;
            taskEntity.job = jobKey.toString();
            taskEntity.persist();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelTask(TaskEntity taskEntity) {

    }
}
