package ru.yandex.practicum.ewm.main.repository.locations;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.ewm.main.model.events.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
