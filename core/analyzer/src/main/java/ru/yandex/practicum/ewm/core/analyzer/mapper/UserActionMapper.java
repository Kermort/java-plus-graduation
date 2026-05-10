package ru.yandex.practicum.ewm.core.analyzer.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.UserActionAvro;
import ru.yandex.practicum.ewm.api.analyzer.enums.UserActionType;
import ru.yandex.practicum.ewm.core.analyzer.config.RecommendationsProperties;
import ru.yandex.practicum.ewm.core.analyzer.model.UserAction;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserActionMapper {
    private final RecommendationsProperties properties;

    public UserAction toEntity(UserActionAvro actionAvro) {
        UserActionType actionType = UserActionType.VIEW;
        switch (actionAvro.getActionType()) {
            case LIKE -> actionType = UserActionType.LIKE;
            case REGISTER -> actionType = UserActionType.REGISTER;
        }

        return UserAction.builder()
                .userId(actionAvro.getUserId())
                .eventId(actionAvro.getEventId())
                .weight(properties.getActionWeight(actionType))
                .actionTime(Instant.now())
                .build();
    }
}
