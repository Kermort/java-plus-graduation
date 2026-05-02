package ru.yandex.practicum.ewm.core.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_action", schema = "ewm_analyzer")
@Builder
public class UserAction {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "weight")
    private double weight;

    @Column(name = "action_time", nullable = false, updatable = false)
    private Instant actionTime;
}
