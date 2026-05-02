package ru.yandex.practicum.ewm.core.analyzer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;
import ru.practicum.ewm.stats.kafka.UserActionAvro;
import ru.yandex.practicum.ewm.core.analyzer.mapper.EventSimilarityMapper;
import ru.yandex.practicum.ewm.core.analyzer.mapper.UserActionMapper;
import ru.yandex.practicum.ewm.core.analyzer.model.EventSimilarity;
import ru.yandex.practicum.ewm.core.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.ewm.core.analyzer.repository.UserActionRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerService {
    private final EventSimilarityRepository esRepository;
    private final UserActionRepository uaRepository;
    private final UserActionMapper userActionMapper;

    @Transactional
    public void processEventSimilarity(EventSimilarityAvro eventSimilarity) {
        log.info("[Analyzer service] process event similarity");
        Optional<EventSimilarity> fromBaseOpt = esRepository.findByEventAidAndEventBid(eventSimilarity.getEventA(), eventSimilarity.getEventB());
        if (fromBaseOpt.isEmpty()) {
            esRepository.save(EventSimilarityMapper.toEntity(eventSimilarity));
        } else {
            EventSimilarity updated = EventSimilarityMapper.toEntity(eventSimilarity);
            updated.setId(fromBaseOpt.get().getId());
            esRepository.save(updated);
        }
    }

    @Transactional
    public void processUserAction(UserActionAvro action) {
        log.info("[Analyzer service] process user action");
        uaRepository.save(userActionMapper.toEntity(action));


    }
}
