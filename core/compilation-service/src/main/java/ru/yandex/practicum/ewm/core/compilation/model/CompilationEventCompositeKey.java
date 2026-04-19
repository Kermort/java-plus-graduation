package ru.yandex.practicum.ewm.core.compilation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationEventCompositeKey implements Serializable {
    private Long compilationId;
    private Long eventId;
}
