package ru.yandex.practicum.ewm.core.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.ewm.core.compilation.model.Compilation;
import ru.yandex.practicum.ewm.api.compilation.dto.CompilationDto;
import ru.yandex.practicum.ewm.api.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.ewm.api.event.dto.EventShortDto;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public static Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public static CompilationDto toDto(Compilation model, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(model.getId())
                .pinned(model.isPinned())
                .title(model.getTitle())
                .events(events)
                .build();
    }
}
