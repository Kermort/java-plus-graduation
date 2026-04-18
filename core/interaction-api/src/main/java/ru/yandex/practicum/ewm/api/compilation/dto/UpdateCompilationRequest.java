package ru.yandex.practicum.ewm.api.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateCompilationRequest {
    private Set<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50)
    private String title;
}
