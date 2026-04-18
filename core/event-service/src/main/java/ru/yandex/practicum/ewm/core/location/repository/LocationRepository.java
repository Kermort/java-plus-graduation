package ru.yandex.practicum.ewm.core.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.ewm.core.location.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
