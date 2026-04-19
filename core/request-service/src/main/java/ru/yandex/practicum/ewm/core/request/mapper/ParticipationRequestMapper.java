package ru.yandex.practicum.ewm.core.request.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.request.model.ParticipationRequest;
import ru.yandex.practicum.ewm.api.request.dto.ParticipationRequestDto;

@UtilityClass
public class ParticipationRequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest model) {
        return ParticipationRequestDto.builder()
                .id(model.getId())
                .created(model.getCreated())
                .event(model.getEventId())
                .requester(model.getRequesterId())
                .status(model.getStatus().toString())
                .build();
    }
}
