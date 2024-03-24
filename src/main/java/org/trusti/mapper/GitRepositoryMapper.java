package org.trusti.mapper;

import org.mapstruct.Mapper;
import org.trusti.dto.SourceDto;
import org.trusti.models.jpa.entity.SourceEntity;

@Mapper(componentModel = "cdi")
public interface GitRepositoryMapper {

    SourceDto toDto(SourceEntity entity);

}
