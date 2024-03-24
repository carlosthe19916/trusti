package org.trusti.models.jpa;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.trusti.models.AdvisoryMetadata;
import org.trusti.models.jpa.entity.AdvisoryEntity;

@Transactional
@ApplicationScoped
public class AdvisoryRepository implements PanacheRepository<AdvisoryEntity> {

    public AdvisoryEntity create(AdvisoryMetadata metadata) {
        AdvisoryEntity advisoryEntity = new AdvisoryEntity();
        advisoryEntity.identifier = metadata.identifier();
        advisoryEntity.title = metadata.title();
        advisoryEntity.severity = metadata.severity();
        advisoryEntity.releaseDate = metadata.releaseDate();

        advisoryEntity.persist();
        return advisoryEntity;
    }
}
