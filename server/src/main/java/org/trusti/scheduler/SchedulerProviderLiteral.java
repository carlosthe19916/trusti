package org.trusti.scheduler;

import jakarta.enterprise.util.AnnotationLiteral;

public class SchedulerProviderLiteral extends AnnotationLiteral<SchedulerProviderType> implements SchedulerProviderType {

    private final SchedulerProviderType.Type type;

    public SchedulerProviderLiteral(SchedulerProviderType.Type type) {
        this.type = type;
    }

    @Override
    public SchedulerProviderType.Type value() {
        return type;
    }
}
