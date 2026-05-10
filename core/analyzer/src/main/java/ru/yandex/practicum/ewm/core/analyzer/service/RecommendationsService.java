package ru.yandex.practicum.ewm.core.analyzer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.core.analyzer.model.EventSimilarity;
import ru.yandex.practicum.ewm.core.analyzer.model.Recommendation;
import ru.yandex.practicum.ewm.core.analyzer.model.UserAction;
import ru.yandex.practicum.ewm.core.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.ewm.core.analyzer.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsService {
    private final EventSimilarityRepository esRepository;
    private final UserActionRepository uaRepository;

    @Transactional
    public List<Recommendation> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("[Recommendations service] get Similar event for eventId={}, userId={}, maxResults={}", eventId, userId, maxResults);
        //все события, для которых рассчитаны коэффициенты похожести
        List<EventSimilarity> allEventSimilarities = esRepository.findByEventAidOrEventBid(eventId);

        //все рекомендации (пара событие-коэффициент)
        List<Recommendation> allRecommendations = allEventSimilarities.stream()
                .map(es -> {
                    if (es.getEventAid() == eventId) {
                        return new Recommendation(es.getEventBid(), es.getScore());
                    } else {
                        return new Recommendation(es.getEventAid(), es.getScore());
                    }
                })
                .toList();

        //все действия пользователя
        List<UserAction> userActions = uaRepository.findByUserId(userId);

        //все события (id), с которыми взаимодействовал пользователь
        Set<Long> interactedEventIds = userActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        //рекомендации по событиям, отсутствующим в списке тех, с которыми взаимодействовал пользователь
        List<Recommendation> notInteracted = allRecommendations.stream()
                .filter(r -> !interactedEventIds.contains(r.getEventId()))
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(maxResults)
                .toList();
        log.info("[Recommendation service] {} recommendations found", notInteracted.size());
        return notInteracted;
    }

    @Transactional
    public List<Recommendation> getInteractionsCount(List<Long> eventIds) {
        log.info("[Recommendations service] get interactions for events={}", eventIds);
        List<UserAction> userActions = uaRepository.findInteractionsByEventIds(eventIds);
        Map<Long, UserAction> maxWeightsByUsers = new HashMap<>();

        for (UserAction ua: userActions) {
            maxWeightsByUsers.putIfAbsent(ua.getUserId(), ua);
            if (ua.getWeight() > maxWeightsByUsers.get(ua.getUserId()).getWeight()) {
                maxWeightsByUsers.put(ua.getUserId(), ua);
            }
        }

        log.info("[Recommendations service] userActions size = {}", userActions.size());
        Map<Long, Double> interactionsMap = new HashMap<>();
        for (UserAction ua: maxWeightsByUsers.values()) {
            interactionsMap.putIfAbsent(ua.getEventId(), 0.0);
            interactionsMap.put(ua.getEventId(), interactionsMap.get(ua.getEventId()) + ua.getWeight());
        }
        List<Recommendation> interactions = interactionsMap.entrySet().stream()
                .map(e -> new Recommendation(e.getKey(), e.getValue()))
                .toList();

        log.info("[Recommendations service] Retrieved interaction counts for {} events.", interactions.size());

        return interactions;
    }

    public List<Recommendation> getRecommendationsForUser(long userId, int maxResults) {
        log.info("[Recommendations service] get recommendations for user={}", userId);
        //события (id), с которыми пользователь недавно взаимодействовал
        Set<Long> recentlyInteractedEvents = uaRepository.findRecentlyInteractedEvents(userId, maxResults);
        if (recentlyInteractedEvents.isEmpty()) {
            log.info("[Recommendation service] user {} has not interacted any events", userId);
            return List.of();
        }

        //похожести с событиями, с которыми пользователь еще не взаимодействовал
        Set<EventSimilarity> similarities = esRepository.findByEventIdsAndNotInteracted(recentlyInteractedEvents);

        //события в виде рекомендаций для хранения id события - коэффициент
        List<Recommendation> notInteracted = similarities.stream()
                .map(es -> {
                    if (recentlyInteractedEvents.contains(es.getEventAid())) {
                        return new Recommendation(es.getEventBid(), es.getScore());
                    } else {
                        return new Recommendation(es.getEventAid(), es.getScore());
                    }
                })
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(maxResults)
                .toList();

        List<Recommendation> result = new ArrayList<>();

        for (Recommendation recommendation: notInteracted) {
            List<Recommendation> similarToNotInteractedEvent = getSimilarEvents(recommendation.getEventId(), userId, maxResults);
            if (similarToNotInteractedEvent.isEmpty()) {
                result.add(new Recommendation(recommendation.getEventId(), 0.0));
                continue;
            }
            Map<Long, Double> similarEventsMap = similarToNotInteractedEvent.stream()
                    .collect(Collectors.toMap(
                            Recommendation::getEventId,
                            Recommendation::getScore
                    ));

            List<Long> similarEventIds = similarToNotInteractedEvent.stream()
                    .map(Recommendation::getEventId)
                    .toList();
            List<UserAction> maxUserActions = uaRepository.findInteractionsByEventIdsAndUserId(similarEventIds, userId);
            Map<Long, Double> maxWeightsByUsers = new HashMap<>();
            for (UserAction ua: maxUserActions) {
                maxWeightsByUsers.putIfAbsent(ua.getUserId(), ua.getWeight());
                if (ua.getWeight() > maxWeightsByUsers.get(ua.getUserId())) {
                    maxWeightsByUsers.put(ua.getUserId(), ua.getWeight());
                }
            }

            double sumWeights = 0.0;
            for (Recommendation r: similarToNotInteractedEvent) {
                sumWeights += maxWeightsByUsers.getOrDefault(r.getEventId(), 0.0)
                        * similarEventsMap.getOrDefault(r.getEventId(), 0.0);
            }

            double sumScores = similarEventsMap.values().stream().mapToDouble(Double::doubleValue).sum();

            double prediction = sumWeights / sumScores;
            result.add(new Recommendation(recommendation.getEventId(), prediction));

        }

        return result;
    }
}
