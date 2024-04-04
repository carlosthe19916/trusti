package org.trusti.models.jpa;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.trusti.models.AdvisoryMetadata;
import org.trusti.models.Page;
import org.trusti.models.SearchResult;
import org.trusti.models.SortBy;
import org.trusti.models.jpa.entity.AdvisoryEntity;
import org.trusti.models.jpa.entity.TaskEntity;

import java.util.List;
import java.util.Optional;

@Transactional
@ApplicationScoped
public class AdvisoryRepository implements PanacheRepository<AdvisoryEntity> {

    public static final String[] SORT_BY_FIELDS = {"id", "identifier", "severity", "releaseDate"};

    public static record Filters(List<String> severity) {
    }

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

    public SearchResult<AdvisoryEntity> list(Filters filters, Page page, List<SortBy> sortBy) {
        StringBuilder queryBuilder = new StringBuilder("select e from AdvisoryEntity e");
        Parameters params = new Parameters();

        if (filters.severity() != null && !filters.severity().isEmpty()) {
            queryBuilder.append(" and e.severity in :severity");
            params = params.and("severity", filters.severity());
        }

        Sort sort = Sort.by();
        sortBy.forEach(f -> sort.and(String.format("e.%s", f.field()), f.asc() ? Sort.Direction.Ascending : Sort.Direction.Descending));

        PanacheQuery<AdvisoryEntity> query = AdvisoryEntity
                .find(queryBuilder.toString(), sort, params)
                .range(page.offset(), page.offset() + page.limit() - 1);

        return new SearchResult<>(query.count(), query.list());
    }
}
