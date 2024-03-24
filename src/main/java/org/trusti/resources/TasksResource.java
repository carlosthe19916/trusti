package org.trusti.resources;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.dto.TaskDto;
import org.trusti.mapper.TaskMapper;
import org.trusti.models.TaskState;
import org.trusti.models.jpa.entity.SourceEntity;
import org.trusti.models.jpa.entity.TaskEntity;

import java.util.UUID;

@Transactional
@ApplicationScoped
@Path("/tasks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TasksResource {

    @Inject
    TaskMapper taskMapper;

    @Inject
    Event<TaskDto> taskEvent;

    @Transactional(Transactional.TxType.NEVER)
    @POST
    @Path("/")
    public RestResponse<TaskDto> createTask(TaskDto taskDto) {
        if (taskDto.source() == null) {
            return RestResponse.ResponseBuilder
                    .<TaskDto>create(RestResponse.Status.BAD_REQUEST)
                    .build();
        }

        QuarkusTransaction.begin();

        SourceEntity sourceEntity = SourceEntity.findById(taskDto.source().id());
        if (sourceEntity == null) {
            QuarkusTransaction.commit();
            return RestResponse.ResponseBuilder
                    .<TaskDto>create(RestResponse.Status.NOT_FOUND)
                    .build();
        }

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.name = "task";
        taskEntity.state = TaskState.Created;
        taskEntity.source = sourceEntity;
        taskEntity.persist();

        taskEntity.name = "task-" + taskEntity.id + "-" + UUID.randomUUID();
        taskEntity.persist();

        TaskDto result = taskMapper.toDto(taskEntity);
        QuarkusTransaction.commit();

        taskEvent.fire(result);
        return RestResponse.ResponseBuilder
                .<TaskDto>create(RestResponse.Status.OK)
                .entity(result)
                .build();
    }

    @PUT
    @Path("/{taskId}")
    public RestResponse<TaskDto> updateTask(@PathParam("taskId") Long taskId, TaskDto taskDto) {
        return TaskEntity.<TaskEntity>findByIdOptional(taskId)
                .map(taskEntity -> {
                    taskEntity.state = taskDto.state();

                    taskEntity.persist();
                    return taskMapper.toDto(taskEntity);
                })
                .map(dto -> RestResponse.ResponseBuilder
                        .<TaskDto>create(RestResponse.Status.OK)
                        .entity(dto)
                        .build()
                )
                .orElse(RestResponse.ResponseBuilder
                        .<TaskDto>create(RestResponse.Status.NOT_FOUND)
                        .build()
                );
    }

}
