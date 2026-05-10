package ru.yandex.practicum.ewm.core.aggregator.repository;

import java.util.Map;

public interface UserEventWeightRepository {
    double findWeight(long userId, long eventId);

    void save(long userId, long eventId, double weight);

    Map<Long, Double> findWeightsByUserId(long userId);
}
