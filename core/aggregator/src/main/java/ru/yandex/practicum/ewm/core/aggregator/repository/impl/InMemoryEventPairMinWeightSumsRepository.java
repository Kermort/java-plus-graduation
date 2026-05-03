package ru.yandex.practicum.ewm.core.aggregator.repository.impl;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ewm.core.aggregator.repository.EventPairMinWeightSumsRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryEventPairMinWeightSumsRepository implements EventPairMinWeightSumsRepository {
    //Map<event_A_id, Map<event_B_id, sum>>
    private final Map<Long, Map<Long, Double>> eventPairSums = new ConcurrentHashMap<>();

    @Override
    public Map<Long, Double> updateWithDeltas(long eventId, Map<Long, Double> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> updatedSums = new HashMap<>();

        for (Map.Entry<Long, Double> entry : deltas.entrySet()) {
            long otherEventId = entry.getKey();
            double delta = entry.getValue();

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);

            Map<Long, Double> innerMap = eventPairSums.computeIfAbsent(eventA, k -> new HashMap<>());

            double newSum = innerMap.compute(eventB, (key, currentSum) -> {
                if (currentSum == null) {
                    return delta;
                }
                return currentSum + delta;
            });

            updatedSums.put(otherEventId, newSum);
        }

        return updatedSums;
    }
}
