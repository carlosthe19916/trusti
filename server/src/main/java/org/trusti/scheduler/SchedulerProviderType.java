package org.trusti.scheduler;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Documented
public @interface SchedulerProviderType {
    Type value();

    enum Type {
        KUBERNETES,
        INTERNAL
    }
}
