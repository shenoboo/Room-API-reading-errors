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

    // Many reservations, one account. owner_id is the foreign key (replaces reserved_by).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    private LocalDateTime startTime;  // start_time
    private LocalDateTime endTime;    // end_time

    protected Reservation() { }       // for Hibernate

    public Reservation(Room room, AppUser owner,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.room = room;
        this.owner = owner;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isOwnedBy(String username) {
        return owner.getUsername().equals(username);
    }

    // Lab G: one named change; Hibernate notices it at commit (dirty checking)
    public void reschedule(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public AppUser getOwner() { return owner; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
