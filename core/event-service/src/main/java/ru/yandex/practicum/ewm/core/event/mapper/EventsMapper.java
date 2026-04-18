package ru.yandex.practicum.ewm.core.event.mapper;

import org.mapstruct.*;

import ru.yandex.practicum.ewm.api.event.dto.*;
import ru.yandex.practicum.ewm.api.user.dto.UserShortDto;
import ru.yandex.practicum.ewm.core.event.model.Events;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventsMapper {
    @Mapping(target = "category",
            expression = "java(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))")
    @Mapping(target = "initiator",
            expression = "java(new UserShortDto(userDto.id(), userDto.name()))")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(source = "event.id", target = "id")
    EventShortDto toShortDto(Events event, UserShortDto userDto);

    @Mapping(target = "category",
            expression = "java(new CategoryDto(eventInternalDto.category().getId(), eventInternalDto.category().getName()))")
    @Mapping(target = "initiator",
            expression = "java(userShortDto)")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(source = "eventInternalDto.id", target = "id")
    EventShortDto toShortDto(EventInternalDto eventInternalDto, UserShortDto userShortDto);


    @Mapping(target = "category",
            expression = "java(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))")
    @Mapping(target = "initiator",
            expression = "java(userDto)")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(source = "event.id", target = "id")
    EventFullDto toFullDto(Events event, UserShortDto userDto);

    @Mapping(target = "category",
            expression = "java(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))")
    @Mapping(target = "initiatorId",
            expression = "java(event.getInitiatorId())")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(source = "event.id", target = "id")
    EventInternalDto toInternalDto(Events event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Events toEntity(NewEventDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserRequest(
            UpdateEventUserRequest source,
            @MappingTarget Events target
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminRequest(
            UpdateEventAdminRequest source,
            @MappingTarget Events target
    );
}
