package ru.yandex.practicum.ewm.main.model.request;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.ewm.main.enums.ParticipationRequestStatus;
import ru.yandex.practicum.ewm.main.model.events.Events;
import ru.yandex.practicum.ewm.main.model.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests", schema = "ewm")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "created")
    LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event_id")
    Events event;
    @ManyToOne
    @JoinColumn(name = "requester_id")
    User requester;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    ParticipationRequestStatus status;
}
