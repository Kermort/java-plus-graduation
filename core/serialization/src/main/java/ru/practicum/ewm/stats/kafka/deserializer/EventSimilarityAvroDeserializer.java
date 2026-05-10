package ru.practicum.ewm.stats.kafka.deserializer;

import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;

public class EventSimilarityAvroDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
