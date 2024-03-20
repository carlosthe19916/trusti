package org.trusti.mapper;

import org.mapstruct.Mapper;
import org.trusti.dto.AdvisoryDto;
import org.trusti.models.jpa.entity.AdvisoryEntity;

@Mapper(componentModel = "cdi")
public interface AdvisoryMapper {

    AdvisoryDto toDto(AdvisoryEntity entity);

}
