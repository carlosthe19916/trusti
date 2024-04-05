package org.trusti.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.lang.annotation.Annotation;

@ApplicationScoped
public class Scheduler {

    @ConfigProperty(name = "trusti.scheduler.type")
    String schedulerType;

    @Inject
    @Any
    Instance<SchedulerProvider> schedulers;

    public SchedulerProvider getInstance() {
        SchedulerProviderType.Type providerType = SchedulerProviderType.Type.valueOf(schedulerType.toUpperCase());
        Annotation annotation = new SchedulerProviderLiteral(providerType);
        return schedulers.select(annotation).get();
    }

}
