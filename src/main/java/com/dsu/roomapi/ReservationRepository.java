package com.dsu.roomapi;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // "RoomId" is a path: room.id
    List<Reservation> findByRoomIdOrderByStartTime(Long roomId);

    boolean existsByRoomId(Long roomId);

    // join fetch: one query, room comes back with the reservation (avoids N+1)
    @Query("""
            select r from Reservation r
            join fetch r.room
            where r.reservedBy = :name
            order by r.startTime
            """)
    List<Reservation> findByReservedByWithRoom(@Param("name") String name);

    // new.start < old.end AND new.end > old.start  (touching is fine)
    @Query("""
            select count(r) from Reservation r
            where r.room.id = :roomId
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
