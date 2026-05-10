package ru.yandex.practicum.ewm.core.collector.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.grpc.UserActionProto;
import ru.practicum.ewm.stats.kafka.ActionTypeAvro;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

import java.time.Instant;

@UtilityClass
public class UserActionMapper {
    public static UserActionAvro toAvro(UserActionProto action) {
        ActionTypeAvro actionType;

        switch (action.getActionType()) {
            case ACTION_LIKE -> actionType = ActionTypeAvro.LIKE;
            case ACTION_VIEW -> actionType = ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> actionType = ActionTypeAvro.REGISTER;
            default -> throw new RuntimeException();
        }

        Instant timestamp = Instant.ofEpochSecond(action.getTimestamp().getSeconds(), action.getTimestamp().getNanos());

        return UserActionAvro.newBuilder()
                .setUserId(action.getUserId())
                .setEventId(action.getEventId())
                .setActionType(actionType)
                .setTimestamp(timestamp)
                .build();

    }
}
