package ru.yandex.practicum.ewm.core.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.ewm.core.location.model.Location;
import ru.yandex.practicum.ewm.api.location.dto.LocationDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);
}
