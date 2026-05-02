package ru.yandex.practicum.ewm.core.aggregator.repository;

import java.util.Map;
import java.util.Set;

public interface EventWeightSumRepository {
    double findWeightSum(long eventId);

    void saveWeightSum(long eventId, double sum);

    Map<Long, Double> findWeightSums(Set<Long> eventIds);
}
