package ru.yandex.practicum.ewm.core.aggregator.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;

@Slf4j
@Component
public class EventsSimilarityProducer {
    private final String eventsSimilarityTopic;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public EventsSimilarityProducer(
            @Value("${aggregator.kafka.topic.events-similarity}") String eventsSimilarityTopic,
            KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate) {
        this.eventsSimilarityTopic = eventsSimilarityTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventsSimilarity(EventSimilarityAvro eventSimilarity) {
        log.info("[Controller kafka client] sending user action to Kafka topic '{}': {}", eventsSimilarityTopic, eventSimilarity);

        kafkaTemplate.send(eventsSimilarityTopic, eventSimilarity)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[Controller kafka client] user action sent to offset {}",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[Controller kafka client] failed to send user action: {}", ex.getMessage());
                    }
                });


    }



}
