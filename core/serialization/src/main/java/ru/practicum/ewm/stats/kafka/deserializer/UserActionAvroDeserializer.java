package ru.practicum.ewm.stats.kafka.deserializer;

import ru.practicum.ewm.stats.kafka.UserActionAvro;

public class UserActionAvroDeserializer extends BaseAvroDeserializer<UserActionAvro> {
    public UserActionAvroDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
