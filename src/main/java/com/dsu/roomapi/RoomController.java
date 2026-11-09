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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) Integer minCapacity,
                                   @RequestParam(defaultValue = "") String keyword) {
        return roomService.search(minCapacity, keyword).stream()
                .map(RoomResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public RoomResponse findOne(@PathVariable Long id) {
        return RoomResponse.from(roomService.get(id));
    }

    // POST keeps ResponseEntity: it needs 201 and a Location header
    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomCreateRequest request) {
        Room room = roomService.create(request.name(), request.capacity());
        return ResponseEntity
                .created(URI.create("/api/rooms/" + room.getId()))
                .body(RoomResponse.from(room));
    }

    @PutMapping("/{id}")
    public RoomResponse replace(@PathVariable Long id,
                                @Valid @RequestBody RoomCreateRequest request) {
        return RoomResponse.from(
                roomService.update(id, request.name(), request.capacity()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}
