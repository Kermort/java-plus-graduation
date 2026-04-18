package ru.yandex.practicum.ewm.core.location.service;

import ru.yandex.practicum.ewm.core.location.model.Location;
import ru.yandex.practicum.ewm.api.location.dto.LocationDto;

public interface LocationService {
    Location saveLocation(LocationDto location);
}
