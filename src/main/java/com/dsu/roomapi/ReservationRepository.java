package com.dsu.roomapi;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // "RoomId" is a path: room.id. The graph loads room and owner in the same query (no N+1).
    @EntityGraph(attributePaths = {"room", "owner"})
    List<Reservation> findByRoomIdOrderByStartTime(Long roomId);

    boolean existsByRoomId(Long roomId);

    // One JOIN query: the caller's own reservations, with room and owner
    @Query("""
            select r from Reservation r
            join fetch r.room
            join fetch r.owner
            where r.owner.username = :username
            order by r.startTime
            """)
    List<Reservation> findMine(@Param("username") String username);

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

    // Lab G: like countOverlapping, but a reservation never clashes with itself
    @Query("""
            select count(r) from Reservation r
            where r.room.id = :roomId
              and r.id <> :id
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    long countOverlappingExcept(@Param("roomId") Long roomId,
                                @Param("id") Long id,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
