package ru.yandex.practicum.ewm.core.compilation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "compilation_event", schema = "ewm_compilations")
@IdClass(CompilationEventCompositeKey.class)
public class CompilationEvent {
    @Id
    private Long compilationId;

    @Id
    private Long eventId;
}
