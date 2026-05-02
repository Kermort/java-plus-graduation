package ru.yandex.practicum.ewm.core.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recommendation {
    private long eventId;

    private double score;
}
