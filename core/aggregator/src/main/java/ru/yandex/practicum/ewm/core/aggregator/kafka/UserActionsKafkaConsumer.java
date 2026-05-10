package ru.yandex.practicum.ewm.core.aggregator.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.UserActionAvro;
import ru.yandex.practicum.ewm.core.aggregator.service.SimilarityCalculationService;

@Slf4j
@Component
public class UserActionsKafkaConsumer {
    private final SimilarityCalculationService calcService;

    public UserActionsKafkaConsumer(SimilarityCalculationService calcService) {
        this.calcService = calcService;
    }

    @KafkaListener(topics = "#{'${aggregator.kafka.topic.user-actions}'}")
    public void listen(UserActionAvro message) {
        log.info("[User action kafka consumer] message received {} ", message);
        processMessage(message);
    }

    private void processMessage(UserActionAvro message) {
        log.info("[User action kafka consumer] process message");
        calcService.processMessage(message);
    }

}
