package org.trusti.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.dto.AdvisoryDto;
import org.trusti.mapper.AdvisoryMapper;
import org.trusti.models.Page;
import org.trusti.models.SearchResult;
import org.trusti.models.SortBy;
import org.trusti.models.jpa.AdvisoryRepository;
import org.trusti.models.jpa.entity.AdvisoryEntity;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/advisories")
public class AdvisoriesResource {

    @Inject
    AdvisoryMapper advisoryMapper;

    @Inject
    AdvisoryRepository advisoryRepository;

    @GET
    @Path("/")
    public RestResponse<List<AdvisoryDto>> advisories(
            @QueryParam("q") @DefaultValue("") String query,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("id:desc") List<String> sortBy
    ) {
        Page page = Page.from(offset, limit);
        List<SortBy> sort = SortBy.getSorts(sortBy, AdvisoryRepository.SORT_BY_FIELDS);

        AdvisoryRepository.Filters filters = new AdvisoryRepository.Filters(null);

        SearchResult<AdvisoryEntity> searchResult = advisoryRepository.list(filters, page, sort);

        List<AdvisoryDto> items = searchResult.list().stream()
                .map(entity -> advisoryMapper.toDto(entity))
                .collect(Collectors.toList());

        return RestResponse.ResponseBuilder
                .<List<AdvisoryDto>>create(RestResponse.Status.OK)
                .entity(items)
                .header("X-Total-Count", searchResult.count())
                .build();
    }

}
