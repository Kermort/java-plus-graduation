package ru.yandex.practicum.ewm.core.collector.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

@Slf4j
@Component
public class KafkaClient {
    private final String userActionsTopic;
    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    public KafkaClient(
            @Value("${collector.kafka.topic.user-actions}") String userActionsTopic,
            KafkaTemplate<String, UserActionAvro> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.userActionsTopic = userActionsTopic;
    }

    public void sendUserAction(UserActionAvro action) {
        log.info("[Controller kafka client] sending user action to Kafka topic '{}': {}", userActionsTopic, action);

        kafkaTemplate.send(userActionsTopic, action)
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
