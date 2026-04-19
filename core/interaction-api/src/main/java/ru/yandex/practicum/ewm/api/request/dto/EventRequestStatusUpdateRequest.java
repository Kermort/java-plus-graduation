package ru.yandex.practicum.ewm.api.request.dto;

import lombok.*;
import ru.yandex.practicum.ewm.api.request.enums.ParticipationRequestStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;

    ParticipationRequestStatus status;
}
