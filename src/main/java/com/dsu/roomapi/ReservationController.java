package com.dsu.roomapi;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/rooms/{roomId}/reservations")
    public List<ReservationResponse> listForRoom(@PathVariable Long roomId) {
        return reservationService.findByRoom(roomId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping("/rooms/{roomId}/reservations")
    public ResponseEntity<ReservationResponse> create(
            @PathVariable Long roomId,
            @Valid @RequestBody ReservationCreateRequest request) {
        Reservation r = reservationService.create(roomId, request.reservedBy(),
                request.startTime(), request.endTime());
        return ResponseEntity
                .created(URI.create("/api/reservations/" + r.getId()))
                .body(ReservationResponse.from(r));
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> search(@RequestParam String reservedBy) {
        return reservationService.findByReservedBy(reservedBy).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse findOne(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.get(id));
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reservationService.delete(id);
    }
}
