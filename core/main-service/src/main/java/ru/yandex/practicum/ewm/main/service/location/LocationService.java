package ru.yandex.practicum.ewm.main.service.location;

import ru.yandex.practicum.ewm.main.model.events.Location;
import ru.yandex.practicum.ewm.main.model.events.dto.LocationDto;

public interface LocationService {
    Location saveLocation(LocationDto location);
}
