package com.dsu.roomapi;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many reservations, one room. room_id is the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    private String reservedBy;      // reserved_by
    private LocalDateTime startTime; // start_time
    private LocalDateTime endTime;   // end_time

    protected Reservation() { }     // for Hibernate

    public Reservation(Room room, String reservedBy,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.room = room;
        this.reservedBy = reservedBy;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public String getReservedBy() { return reservedBy; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
