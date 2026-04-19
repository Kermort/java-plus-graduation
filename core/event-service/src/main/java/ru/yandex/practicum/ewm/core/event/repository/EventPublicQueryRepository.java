package ru.yandex.practicum.ewm.core.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.core.event.model.params.PublicEventSearchParams;

public interface EventPublicQueryRepository {
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD,
            attributePaths = {"category", "location", "location"})
    Page<Events> findPublicEvents(PublicEventSearchParams params, Pageable pageable);
}
