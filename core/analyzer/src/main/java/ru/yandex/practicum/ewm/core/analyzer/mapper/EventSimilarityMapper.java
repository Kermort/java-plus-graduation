package ru.yandex.practicum.ewm.core.analyzer.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;
import ru.yandex.practicum.ewm.core.analyzer.model.EventSimilarity;

@UtilityClass
public class EventSimilarityMapper {
    public static EventSimilarity toEntity(EventSimilarityAvro similarityAvro) {
        return EventSimilarity.builder()
                .eventAid(similarityAvro.getEventA())
                .eventBid(similarityAvro.getEventB())
                .score(similarityAvro.getScore())
                .build();
    }
}
