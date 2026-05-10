package ru.practicum.ewm.stats.kafka.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message) {
        super(message);
    }
}
