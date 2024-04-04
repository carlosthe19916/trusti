package org.trusti.mapper;

import org.mapstruct.Mapper;
import org.trusti.dto.TaskDto;
import org.trusti.models.jpa.entity.TaskEntity;

@Mapper(componentModel = "cdi")
public interface TaskMapper {

    TaskDto toDto(TaskEntity entity);

}
