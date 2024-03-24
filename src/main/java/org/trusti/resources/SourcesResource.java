package org.trusti.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.trusti.dto.SourceDto;
import org.trusti.mapper.SourceMapper;
import org.trusti.models.jpa.entity.GitDetailsEntity;
import org.trusti.models.jpa.entity.SourceEntity;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
@Path("/sources")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SourcesResource {

    @Inject
    SourceMapper sourceMapper;

    @POST
    @Path("/")
    public RestResponse<SourceDto> createSource(SourceDto dto) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.type = dto.type();
        sourceEntity.url = dto.url();
        sourceEntity.taskImage = dto.taskImage();

        if (dto.gitDetails() != null) {
            sourceEntity.gitDetails = new GitDetailsEntity();
            sourceEntity.gitDetails.ref = dto.gitDetails().ref();
            sourceEntity.gitDetails.workingDirectory = dto.gitDetails().workingDirectory();
        }
        sourceEntity.persist();

        SourceDto result = sourceMapper.toDto(sourceEntity);
        return RestResponse.ResponseBuilder
                .<SourceDto>create(RestResponse.Status.OK)
                .entity(result)
                .build();
    }

    @GET
    @Path("/")
    public RestResponse<List<SourceDto>> getSources() {
        List<SourceDto> result = SourceEntity.<SourceEntity>findAll().list()
                .stream()
                .map(entity -> sourceMapper.toDto(entity))
                .toList();

        return RestResponse.ResponseBuilder
                .<List<SourceDto>>create(RestResponse.Status.OK)
                .entity(result)
                .build();
    }

}
