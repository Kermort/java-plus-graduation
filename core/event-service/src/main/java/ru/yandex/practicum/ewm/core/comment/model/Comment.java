package ru.yandex.practicum.ewm.core.comment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.ewm.api.comment.enums.CommentStatus;
import ru.yandex.practicum.ewm.core.event.model.Events;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", schema = "ewm_events")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    Events event;

    @Column(name = "user_id", nullable = false)
    Long authorId;

    @Column(name = "comment_text")
    String text;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    CommentStatus status;
}
