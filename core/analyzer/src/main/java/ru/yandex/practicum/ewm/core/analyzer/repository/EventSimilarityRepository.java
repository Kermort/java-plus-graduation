package ru.yandex.practicum.ewm.core.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ewm.core.analyzer.model.EventSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findByEventAidAndEventBid(long eventAid, long eventBid);

    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE es.eventAid = :eventId OR es.eventBid = :eventId""")
    List<EventSimilarity> findByEventAidOrEventBid(@Param("eventId") long eventId);

    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE (es.eventAid IN :eventIds AND es.eventBid NOT IN :eventIds)
               OR (es.eventBid IN :eventIds AND es.eventAid NOT IN :eventIds)""")
    Set<EventSimilarity> findByEventIdsAndNotInteracted(@Param("eventIds")Collection<Long> eventIds);
}
