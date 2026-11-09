package com.dsu.roomapi;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final RoomService roomService;

    public StatsController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/api/stats")
    public Map<String, Long> stats() {
        return Map.of("rooms", roomService.count());
    }
}
