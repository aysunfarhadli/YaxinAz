package com.yaxinaz.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {

    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, AttendanceStatus status);

    @Query("select a.status as status, count(a) as total from EventAttendance a where a.event.id = :eventId group by a.status")
    List<AttendanceStatusCount> countByEventIdGroupByStatus(@Param("eventId") Long eventId);

    interface AttendanceStatusCount {
        AttendanceStatus getStatus();
        Long getTotal();
    }
}
