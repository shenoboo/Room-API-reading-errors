package com.dsu.roomapi;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(RoomRepository roomRepository,
                              ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("room " + roomId + " not found");
        }
        return reservationRepository.findByRoomIdOrderByStartTime(roomId);
    }

    public List<Reservation> findByReservedBy(String reservedBy) {
        return reservationRepository.findByReservedByWithRoom(reservedBy);
    }

    public Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reservation " + id + " not found"));
    }

    // one-field rules (reservedBy, both times present) are checked at the door by @Valid
    @Transactional
    public Reservation create(Long roomId, String reservedBy,
                              LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {                         // two fields: 400
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        Room room = roomRepository.findById(roomId)                 // 404
                .orElseThrow(() -> new NotFoundException("room " + roomId + " not found"));

        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException(                            // 409
                    "room " + roomId + " is already booked at that time");
        }
        return reservationRepository.save(
                new Reservation(room, reservedBy, startTime, endTime));
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(get(id));
    }
}
