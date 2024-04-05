package org.trusti.scheduler.internal;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionExceptionResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.trusti.importer.ImporterCamelHeaders;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.TaskEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@ApplicationScoped
public class InternalTaskExecutor {

    private static final Logger LOGGER = Logger.getLogger(InternalTaskExecutor.class);

    @Inject
    ProducerTemplate producerTemplate;

    @ConfigProperty(name = "trusti.scheduler.internal.watchDelay")
    Long watchDelay;

    @ConfigProperty(name = "trusti.scheduler.internal.workspace")
    String workspace;

    @Transactional(Transactional.TxType.NEVER)
    public void runTask(Long taskId) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

        LOGGER.info("Init import task");
        Future<?> taskFuture = executorService.submit(() -> initTask(taskId, executorService));

        LOGGER.info("Init import task :: watcher");
        executorService.scheduleAtFixedRate(() -> watchTaskStatus(taskId, executorService, taskFuture), 15, watchDelay, TimeUnit.SECONDS);

        LOGGER.infof("Waiting for all tasks for Task %s to be completed", taskId);
        try {
            boolean terminated = executorService.awaitTermination(6, TimeUnit.HOURS);
            if (terminated) {
                LOGGER.infof("Executor service for Task %s gracefully terminated", taskId);
            } else {
                LOGGER.errorf("Executor service for Task %s could not terminate in the expected time", taskId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void watchTaskStatus(Long taskId, ExecutorService executorService, Future<?> importTask) {
        boolean isTaskBeingCancelled = QuarkusTransaction.requiringNew()
                .timeout(10)
                .exceptionHandler((throwable) -> TransactionExceptionResult.ROLLBACK)
                .call(() -> {
                    TaskEntity watchedTask = TaskEntity.findById(taskId);
                    return watchedTask.state.equals(TaskState.Canceling);
                });

        if (isTaskBeingCancelled) {
            LOGGER.infof("Found Task %s in CANCELLING state. Shutting down import task", taskId);
            importTask.cancel(true);

            QuarkusTransaction.requiringNew()
                    .timeout(10)
                    .exceptionHandler((throwable) -> TransactionExceptionResult.ROLLBACK)
                    .call(() -> {
                        TaskEntity watchedTask = TaskEntity.findById(taskId);
                        return watchedTask.state.equals(TaskState.Canceled);
                    });

            LOGGER.infof("Shutting down Task %s", taskId);
            shutdownExecutorService(executorService);
        }
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdownNow();
    }

    private void initTask(Long taskId, ScheduledExecutorService executorService) {
        TaskEntity taskEntity = TaskEntity.findById(taskId);

        Map<String, Object> headers = new HashMap<>();
        switch (taskEntity.source.type) {
            case http -> {
                headers = ImporterCamelHeaders.http(taskEntity.id, taskEntity.source.url);
            }
            case git -> {
                String repository = taskEntity.source.url;
                String ref = null;
                String workingDirectory = null;

                if (taskEntity.source.gitDetails != null) {
                    ref = taskEntity.source.gitDetails.ref;
                    workingDirectory = taskEntity.source.gitDetails.workingDirectory;
                }

                headers = ImporterCamelHeaders.git(taskEntity.id, workspace, repository, ref, workingDirectory);
            }
        }

        producerTemplate.requestBodyAndHeaders("direct:start-importer", null, headers);

        // Finish
        LOGGER.infof("Import task finished successfully. Shutting down all import tasks for Task %s", taskId);
        shutdownExecutorService(executorService);
    }
}
