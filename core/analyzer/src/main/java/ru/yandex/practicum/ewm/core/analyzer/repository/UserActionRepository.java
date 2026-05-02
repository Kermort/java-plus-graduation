package ru.yandex.practicum.ewm.core.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.ewm.core.analyzer.model.UserAction;

import java.util.List;
import java.util.Set;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    List<UserAction> findByUserId(long userId);

    @Query("SELECT ua FROM UserAction ua " +
        "WHERE ua.weight = (SELECT MAX(ua2.weight) FROM UserAction ua2 " +
        "WHERE ua2.userId = ua.userId AND ua2.eventId = ua.eventId) " +
        "AND ua.eventId IN :eventIds")
    List<UserAction> findInteractionsByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT ua FROM UserAction ua " +
            "WHERE ua.weight = (SELECT MAX(ua2.weight) FROM UserAction ua2 " +
            "WHERE ua2.userId = ua.userId AND ua2.eventId = ua.eventId) " +
            "AND ua.eventId IN :eventIds " +
            "AND ua.userId = :userId")
    List<UserAction> findInteractionsByEventIdsAndUserId(@Param("eventIds") List<Long> eventIds, @Param("userId") long userId);

    @Query("SELECT ua.eventId " +
            "FROM UserAction ua " +
            "WHERE ua.eventId = :eventId " +
            "ORDER BY ua.actionTime DESC " +
            "LIMIT :limit")
    Set<Long> findRecentlyInteractedEvents(@Param("eventId") long eventId, @Param("limit") int limit);
}
