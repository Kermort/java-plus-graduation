package ru.yandex.practicum.ewm.core.analyzer.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.grpc.RecommendedEventProto;
import ru.yandex.practicum.ewm.core.analyzer.model.Recommendation;

@UtilityClass
public class RecommendationsMapper {
    public static RecommendedEventProto toProto(Recommendation recommendation) {
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendation.getEventId())
                .setScore(recommendation.getScore())
                .build();
    }
}
