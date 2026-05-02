package ru.yandex.practicum.ewm.core.aggregator.repository.impl;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ewm.core.aggregator.repository.EventWeightSumRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class InMemoryEventWeightSumRepository implements EventWeightSumRepository {
    //Map<eventId, weightSum>
    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    @Override
    public double findWeightSum(long eventId) {
        return eventWeightSums.getOrDefault(eventId, 0.0);
    }

    @Override
    public void saveWeightSum(long eventId, double sum) {
        eventWeightSums.put(eventId, sum);
    }

    @Override
    public Map<Long, Double> findWeightSums(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return eventIds.stream()
                .collect(Collectors.toConcurrentMap(
                        Function.identity(),
                        eventId -> eventWeightSums.getOrDefault(eventId, 0.0)
                ));
    }
}
