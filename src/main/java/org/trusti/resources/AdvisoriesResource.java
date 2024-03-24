package org.trusti.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.dto.AdvisoryDto;
import org.trusti.mapper.AdvisoryMapper;
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

    @GET
    @Path("/")
    public RestResponse<List<AdvisoryDto>> advisories() {
        List<AdvisoryDto> result = AdvisoryEntity.<AdvisoryEntity>listAll()
                .stream().map(e -> advisoryMapper.toDto(e))
                .collect(Collectors.toList());
        return RestResponse.ok(result);
    }

}
