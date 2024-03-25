package org.trusti.models.jpa;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.trusti.models.AdvisoryMetadata;
import org.trusti.models.jpa.entity.AdvisoryEntity;
import org.trusti.models.jpa.entity.TaskEntity;

import java.util.Optional;

@Transactional
@ApplicationScoped
public class AdvisoryRepository implements PanacheRepository<AdvisoryEntity> {

    public AdvisoryEntity create(AdvisoryMetadata metadata, Long taskId) {
        Optional<TaskEntity> taskEntityOptional = Optional.ofNullable(taskId).map(e -> TaskEntity.findById(taskId));

        AdvisoryEntity advisoryEntity = new AdvisoryEntity();
        advisoryEntity.identifier = metadata.identifier();
        advisoryEntity.title = metadata.title();
        advisoryEntity.severity = metadata.severity();
        advisoryEntity.releaseDate = metadata.releaseDate();

        taskEntityOptional.ifPresent(taskEntity -> advisoryEntity.task = taskEntity);

        advisoryEntity.persist();
        return advisoryEntity;
    }
}
