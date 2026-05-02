package ru.yandex.practicum.ewm.core.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;
import ru.yandex.practicum.ewm.core.analyzer.service.AnalyzerService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {
    private final AnalyzerService analyzerService;

    @KafkaListener(
            id = "similarity-listener",
            containerFactory = "similarityContainerFactory",
            topics = "${analyzer.kafka.topic.events-similarity}"
    )
    public void consumeSimilarity(EventSimilarityAvro eventSimilarity) {
        log.info("[Event similarity kafka consumer] Received similarity data for events {} and {}", eventSimilarity.getEventA(), eventSimilarity.getEventB());
        analyzerService.processEventSimilarity(eventSimilarity);
    }
}
