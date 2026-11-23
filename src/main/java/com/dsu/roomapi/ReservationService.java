package com.dsu.roomapi;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final AppUserRepository userRepository;

    public ReservationService(RoomRepository roomRepository,
                              ReservationRepository reservationRepository,
                              AppUserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public List<Reservation> findByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("room " + roomId + " not found");
        }
        return reservationRepository.findByRoomIdOrderByStartTime(roomId);
    }

    // C3: the caller's own reservations
    public List<Reservation> findMine(String username) {
        return reservationRepository.findMine(username);
    }

    // C4: the owner or an admin
    public Reservation get(Long id, String username, boolean admin) {
        Reservation r = find(id);                         // 404 first
        requireOwnerOrAdmin(r, id, username, admin);      // then 403
        return r;
    }

    // C1: the owner comes from the login, not from the request body
    @Transactional
    public Reservation create(Long roomId, String username,
                              LocalDateTime startTime, LocalDateTime endTime) {
        checkTimes(startTime, endTime);                                   // 400
        Room room = roomRepository.findById(roomId)                       // 404
                .orElseThrow(() -> new NotFoundException("room " + roomId + " not found"));

        if (reservationRepository.countOverlapping(roomId, startTime, endTime) > 0) {
            throw new ConflictException(                                  // 409
                    "room " + roomId + " is already booked at that time");
        }
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user " + username + " not found"));
        return reservationRepository.save(new Reservation(room, owner, startTime, endTime));
    }

    // C5 (Lab G): the owner only. Not even an admin may move someone's booking.
    // Order: 400, 404, 403, 409. No save(): dirty checking sends the UPDATE at commit.
    @Transactional
    public Reservation update(Long id, String username,
                              LocalDateTime startTime, LocalDateTime endTime) {
        checkTimes(startTime, endTime);                                   // 400
        Reservation r = find(id);                                         // 404
        requireOwner(r, id, username);                                    // 403
        Long roomId = r.getRoom().getId();
        if (reservationRepository.countOverlappingExcept(roomId, id, startTime, endTime) > 0) {
            throw new ConflictException(                                  // 409
                    "room " + roomId + " is already booked at that time");
        }
        r.reschedule(startTime, endTime);
        return r;
    }

    // C6: the owner or an admin
    @Transactional
    public void delete(Long id, String username, boolean admin) {
        Reservation r = find(id);                         // 404 first
        requireOwnerOrAdmin(r, id, username, admin);      // then 403
        reservationRepository.delete(r);
    }

    private Reservation find(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reservation " + id + " not found"));
    }

    private void checkTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {                               // two fields
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

    private void requireOwnerOrAdmin(Reservation r, Long id, String username, boolean admin) {
        if (!admin && !r.isOwnedBy(username)) {
            throw new ForbiddenException("reservation " + id + " belongs to another user");
        }
    }

    private void requireOwner(Reservation r, Long id, String username) {
        if (!r.isOwnedBy(username)) {
            throw new ForbiddenException("reservation " + id + " belongs to another user");
        }
    }
}
