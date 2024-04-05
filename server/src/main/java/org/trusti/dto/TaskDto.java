package org.trusti.dto;

import org.trusti.importer.ImporterTaskDto;
import org.trusti.models.TaskState;

import java.util.Date;

public record TaskDto(
        Long id,
        String name,
        TaskState state,
        SourceDto source,
        Date createTime,
        Date started,
        Date terminated,
        String job,
        String error,
        String image
) {

    public static TaskDto from(ImporterTaskDto taskDto) {
        return new TaskDto(
                null,
                null,
                TaskState.valueOf(taskDto.state().toString()),
                null,
                null,
                taskDto.started(),
                taskDto.terminated(),
                null,
                taskDto.error(),
                null
        );
    }
}
