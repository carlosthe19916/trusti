package org.trusti.dto;

import org.trusti.models.TaskState;

public record TaskDto(
        Long id,
        String name,
        TaskState state,
        SourceDto source
) {
}
