package ru.yandex.practicum.ewm.core.aggregator.repository;

import java.util.Map;

public interface EventPairMinWeightSumsRepository {
    Map<Long, Double> updateWithDeltas(long eventId, Map<Long, Double> deltas);
}
