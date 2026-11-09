package com.dsu.roomapi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByNameContainingIgnoreCase(String keyword);
    List<Room> findByCapacityGreaterThanEqualAndNameContainingIgnoreCase(
            int minCapacity, String keyword);
}

