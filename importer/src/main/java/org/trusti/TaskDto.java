package org.trusti;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
public record TaskDto(
        TaskState state,
        Date started,
        Date terminated,
        String error
) {

    @RegisterForReflection
    public enum TaskState {
        Not_supported,
        Canceled,
        Created,
        Succeeded,
        Failed,
        Running,
        No_task,
        Ready,
        Pending,
        Postponed
    }
}
