package ru.yandex.practicum.ewm.core.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.UserActionAvro;
import ru.yandex.practicum.ewm.core.analyzer.service.AnalyzerService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {
    private final AnalyzerService analyzerService;

    @KafkaListener(
            id = "action-listener",
            containerFactory = "actionContainerFactory",
            topics = "${analyzer.kafka.topic.user-actions}"
    )
    public void consumeAction(UserActionAvro action) {
        log.info("[User action kafka consumer] Received action from user {} on event {}", action.getUserId(), action.getEventId());
        analyzerService.processUserAction(action);
    }
}
