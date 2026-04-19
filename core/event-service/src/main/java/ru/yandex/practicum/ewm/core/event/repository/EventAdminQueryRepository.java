package ru.yandex.practicum.ewm.core.event.repository;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.ewm.core.event.model.Events;
import ru.yandex.practicum.ewm.core.event.model.params.AdminEventSearchParams;

import java.util.List;

public interface EventAdminQueryRepository {
    List<Events> findAdminEvents(AdminEventSearchParams params, Pageable pageable);
}
