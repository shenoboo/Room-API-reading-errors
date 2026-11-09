package com.dsu.roomapi;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository,
                       ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Room> search(Integer minCapacity, String keyword) {
        if (minCapacity == null) {
            return roomRepository.findByNameContainingIgnoreCase(keyword);
        }
        return roomRepository.findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
                minCapacity, keyword);
    }

    public Room get(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("room " + id + " not found"));
    }

    // capacity 1..20 is checked at the door (@Valid); the CHECK constraint is the last guard
    public Room create(String name, int capacity) {
        return roomRepository.save(new Room(null, name, capacity));
    }

    // Dirty checking: load a managed entity, change it, no save().
    // The UPDATE (if anything changed) is sent when the transaction commits.
    @Transactional
    public Room update(Long id, String name, int capacity) {
        Room room = get(id);            // managed
        room.update(name, capacity);    // changed in memory
        return room;                    // UPDATE at commit
    }

    @Transactional
    public void delete(Long id) {
        Room room = get(id);
        if (reservationRepository.existsByRoomId(id)) {
            throw new ConflictException("room " + id + " has reservations");
        }
        roomRepository.delete(room);
    }

    public long count() {
        return roomRepository.count();
    }
}
