package ru.yandex.practicum.ewm.core.aggregator.repository.impl;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ewm.core.aggregator.repository.UserEventWeightRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserEventWeightRepository implements UserEventWeightRepository {
    //Map<userId, Map<eventId, weight>>
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    @Override
    public double findWeight(long userId, long eventId) {
        Map<Long, Double> eventWeights = userEventWeights.get(userId);
        if (eventWeights == null) {
            return 0.0;
        }
        return eventWeights.getOrDefault(eventId, 0.0);
    }

    @Override
    public void save(long userId, long eventId, double weight) {
        userEventWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(eventId, weight);
    }

    @Override
    public Map<Long, Double> findWeightsByUserId(long userId) {
        return Collections.unmodifiableMap(userEventWeights.getOrDefault(userId, Collections.emptyMap()));
    }

}
