package org.trusti.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import oasis.csaf.AggregateSeverity;
import oasis.csaf.CsafJsonSchema;
import oasis.csaf.Document;
import oasis.csaf.Tracking;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.dto.AdvisoryDto;
import org.trusti.mapper.AdvisoryMapper;
import org.trusti.models.jpa.entity.AdvisoryEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    @POST
    @Path("/csaf")
    public RestResponse<AdvisoryDto> importCSAF(CsafJsonSchema csaf) {
        Optional<String> identifier = Optional.of(csaf.getDocument()).map(Document::getTracking).map(Tracking::getId);
        Optional<String> title = Optional.of(csaf.getDocument()).map(Document::getTitle);
        Optional<String> aggregateSeverity = Optional.of(csaf.getDocument()).map(Document::getAggregateSeverity).map(AggregateSeverity::getText);
        Optional<Date> currentReleaseDate = Optional.of(csaf.getDocument()).map(Document::getTracking).map(Tracking::getCurrentReleaseDate);

        if (identifier.isEmpty() || title.isEmpty() || aggregateSeverity.isEmpty()) {
            return RestResponse.ResponseBuilder
                    .<AdvisoryDto>create(RestResponse.Status.BAD_REQUEST)
                    .build();
        }

        AdvisoryEntity advisoryEntity = new AdvisoryEntity();
        advisoryEntity.identifier = identifier.orElse(null);
        advisoryEntity.title = title.orElse(null);
        advisoryEntity.severity = aggregateSeverity.orElse(null);
        advisoryEntity.releaseDate = currentReleaseDate.orElse(null);

        advisoryEntity.persist();

        return RestResponse.ResponseBuilder
                .<AdvisoryDto>create(RestResponse.Status.CREATED)
                .entity(advisoryMapper.toDto(advisoryEntity))
                .build();
    }

}
