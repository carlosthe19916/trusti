package org.trusti.tasks;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.trusti.dto.TaskDto;

@ApplicationScoped
public class TaskWatcher {

    @Inject
    ProducerTemplate producerTemplate;

    public void onEvent(@Observes TaskDto taskDto) {
        producerTemplate.requestBodyAndHeaders("direct:create-task", taskDto, null);
    }

}
