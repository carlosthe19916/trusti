package org.trusti.dto;

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
}
