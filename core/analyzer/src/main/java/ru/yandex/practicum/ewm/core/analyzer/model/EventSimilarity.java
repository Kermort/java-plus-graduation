package ru.yandex.practicum.ewm.core.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_similarity", schema = "ewm_analyzer")
@Builder
public class EventSimilarity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_aid", nullable = false)
    private Long eventAid;

    @Column(name = "event_bid", nullable = false)
    private Long eventBid;

    @Column(name = "score", nullable = false)
    private Double score;
}
