package com.dsu.roomapi;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            @Valid @RequestBody ReservationCreateRequest request,
            Authentication authentication) {
        Reservation r = reservationService.create(roomId, authentication.getName(),
                request.startTime(), request.endTime());
        return ResponseEntity
                .created(URI.create("/api/reservations/" + r.getId()))
                .body(ReservationResponse.from(r));
    }

    // replaces ?reservedBy=. A fixed path wins over /{id}, so "mine" is never read as an id.
    @GetMapping("/reservations/mine")
    public List<ReservationResponse> mine(Authentication authentication) {
        return reservationService.findMine(authentication.getName()).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse findOne(@PathVariable Long id, Authentication authentication) {
        return ReservationResponse.from(
                reservationService.get(id, authentication.getName(), isAdmin(authentication)));
    }

    // Lab G: owner only
    @PutMapping("/reservations/{id}")
    public ReservationResponse move(@PathVariable Long id,
                                    @Valid @RequestBody ReservationCreateRequest request,
                                    Authentication authentication) {
        return ReservationResponse.from(reservationService.update(id,
                authentication.getName(), request.startTime(), request.endTime()));
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        reservationService.delete(id, authentication.getName(), isAdmin(authentication));
    }

    // Works for Basic and for tokens: both give ROLE_ authorities.
    private static boolean isAdmin(Authentication a) {
        return a.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
    }
}
