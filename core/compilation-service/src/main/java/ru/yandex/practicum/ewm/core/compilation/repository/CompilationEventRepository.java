package ru.yandex.practicum.ewm.core.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.ewm.core.compilation.model.CompilationEvent;
import ru.yandex.practicum.ewm.core.compilation.model.CompilationEventCompositeKey;

import java.util.List;

public interface CompilationEventRepository extends JpaRepository<CompilationEvent, CompilationEventCompositeKey> {
    @Query("""
            SELECT ce
            FROM CompilationEvent ce
            WHERE ce.compilationId IN :compilationIds
            """)
    List<CompilationEvent> findByCompilationIds(@Param("compilationIds") List<Long> compilationIds);

    @Query("""
            SELECT ce.eventId
            FROM CompilationEvent ce
            WHERE ce.compilationId = :compilationId
            """)
    List<Long> findEventIdsByCompilationId(@Param("compilationId") Long compilationId);
}
