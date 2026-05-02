package ru.yandex.practicum.ewm.core.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.kafka.ActionTypeAvro;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;
import ru.practicum.ewm.stats.kafka.UserActionAvro;
import ru.yandex.practicum.ewm.core.aggregator.kafka.EventsSimilarityProducer;
import ru.yandex.practicum.ewm.core.aggregator.repository.EventPairMinWeightSumsRepository;
import ru.yandex.practicum.ewm.core.aggregator.repository.EventWeightSumRepository;
import ru.yandex.practicum.ewm.core.aggregator.repository.UserEventWeightRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SimilarityCalculationService {
    private final EventWeightSumRepository eventWeightSumRepository;
    private final UserEventWeightRepository userEventWeightRepository;
    private final EventPairMinWeightSumsRepository eventPairMinWeightSumsRepository;
    private final EventsSimilarityProducer producer;

    @Value("${aggregator.recommendations.action-weights.VIEW}")
    private double viewWeight = 0.4;

    @Value("${aggregator.recommendations.action-weights.REGISTER}")
    private double registerWeight = 0.8;

    @Value("${aggregator.recommendations.action-weights.LIKE}")
    private double likeWeight = 1.0;

    public SimilarityCalculationService(
            EventWeightSumRepository eventWeightSumRepository,
            UserEventWeightRepository userEventWeightRepository,
            EventPairMinWeightSumsRepository eventPairMinWeightSumsRepository,
            EventsSimilarityProducer producer) {
        this.eventWeightSumRepository = eventWeightSumRepository;
        this.userEventWeightRepository = userEventWeightRepository;
        this.eventPairMinWeightSumsRepository = eventPairMinWeightSumsRepository;
        this.producer = producer;
    }


    public void processMessage(UserActionAvro message) {
        //получаем сохраненный вес
        double savedWeight = userEventWeightRepository.findWeight(message.getUserId(), message.getEventId());
        double newWeight = viewWeight;
        if (message.getActionType() == ActionTypeAvro.REGISTER) {
            newWeight = registerWeight;
        } else if (message.getActionType() == ActionTypeAvro.LIKE) {
            newWeight = likeWeight;
        }
        if (newWeight <= savedWeight) {
            //записанный вес больше или равен новому. пересчет не требуется
            return;
        }

        //вычисляем разницу между сохраненным весом и пришедшем в сообщении
        double weightDelta = newWeight - savedWeight;
        //прибавляем разницу к сумме весов события
        double newWeightSum = eventWeightSumRepository.findWeightSum(message.getEventId()) + weightDelta;
        //обновляем вес события для пользователя в хранилище
        userEventWeightRepository.save(message.getUserId(), message.getEventId(), newWeight);
        //обновляем сумму весов для события
        eventWeightSumRepository.saveWeightSum(message.getEventId(), newWeightSum);
        //получаем веса по событиям, с которыми взаимодействовал пользователь Map<eventId, weight>
        Map<Long, Double> userEventWeights = userEventWeightRepository.findWeightsByUserId(message.getUserId());
        if (userEventWeights.size() <= 1) {
            log.warn("Пользователь {} взаимодействовал только с одним событием ({}). Проводить расчет нецелесообразно.", message.getUserId(), message.getEventId());
            return;
        }

        Map<Long, Double> minWeightDeltas = new HashMap<>();

        for (Map.Entry<Long, Double> entry : userEventWeights.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == message.getEventId()) {
                continue;
            }
            double otherEventWeight = entry.getValue();
            log.debug("Processing other event {} with weight {} for user {}", otherEventId, otherEventWeight, message.getUserId());


            double oldMin = Math.min(savedWeight, otherEventWeight);
            double newMin = Math.min(newWeight, otherEventWeight);

            minWeightDeltas.put(otherEventId, Math.max(newMin - oldMin, 0.0));
        }


        Map<Long, Double> minWeightSums = eventPairMinWeightSumsRepository.updateWithDeltas(message.getEventId(), minWeightDeltas);
        Map<Long, Double> weightSums = eventWeightSumRepository.findWeightSums(minWeightDeltas.keySet());

        for (Long otherEventId : minWeightDeltas.keySet()) {
            long eventA = Math.min(message.getEventId(), otherEventId);
            long eventB = Math.max(message.getEventId(), otherEventId);

            double otherEventWeightSum = weightSums.get(otherEventId);
            double minWeightSum = minWeightSums.get(otherEventId);
            double similarity = minWeightSum / Math.sqrt(newWeightSum * otherEventWeightSum);
            log.debug("Calculated similarity for pair ({}, {}): {} (newMinSum: {}, newWeightSum: {}, otherEventWeightSum: {})",
                    eventA, eventB, similarity, minWeightSum, newWeightSum, otherEventWeightSum);

            EventSimilarityAvro avroMessage = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(similarity)
                    .setTimestamp(Instant.now())
                    .build();

            producer.sendEventsSimilarity(avroMessage);
        }
    }
}
