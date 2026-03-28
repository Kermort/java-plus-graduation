package ru.yandex.practicum.ewm.main.model.request.dto;

import lombok.*;
import ru.yandex.practicum.ewm.main.enums.ParticipationRequestStatus;

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
