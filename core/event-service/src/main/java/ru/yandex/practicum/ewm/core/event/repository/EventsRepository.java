package ru.yandex.practicum.ewm.core.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.api.event.enums.EventState;

import java.util.List;
import java.util.Optional;

public interface EventsRepository extends JpaRepository<Events, Long>,
        EventPublicQueryRepository, EventAdminQueryRepository {
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD,
            attributePaths = {"category", "location"})
    Page<Events> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Events> findByIdAndState(Long eventId, EventState state);

    List<Events> findByIdIn(List<Long> ids);
}
