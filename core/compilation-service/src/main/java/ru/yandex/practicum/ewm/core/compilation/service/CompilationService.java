package ru.yandex.practicum.ewm.core.compilation.service;

import ru.yandex.practicum.ewm.api.compilation.dto.CompilationDto;
import ru.yandex.practicum.ewm.api.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.ewm.api.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.ewm.core.compilation.model.params.PublicCompilationSearchParams;

import java.util.List;

public interface CompilationService {
    CompilationDto save(NewCompilationDto newCompilationDto);

    CompilationDto patch(UpdateCompilationRequest updateCompilationRequestDto, Long compId);

    void delete(Long compId);

    CompilationDto findById(Long compId);

    List<CompilationDto> findCompilations(PublicCompilationSearchParams params);
}
