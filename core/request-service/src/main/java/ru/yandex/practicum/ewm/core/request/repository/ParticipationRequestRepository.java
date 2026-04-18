package ru.yandex.practicum.ewm.core.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.ewm.api.request.enums.ParticipationRequestStatus;
import ru.yandex.practicum.ewm.core.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> ids);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    @Query("SELECT r.eventId, COUNT(r) " +
           "FROM ParticipationRequest r " +
           "WHERE r.eventId IN :eventIds AND r.status = :status " +
           "GROUP BY r.eventId")
    List<Object[]> countByEventIdsAndStatus(List<Long> eventIds, ParticipationRequestStatus status);
}
