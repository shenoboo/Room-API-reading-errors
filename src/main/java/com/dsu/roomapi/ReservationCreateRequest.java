package com.dsu.roomapi;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;

// No reservedBy: the owner is the logged-in account. Nothing to type, nothing to fake.
// A "reservedBy" sent anyway is ignored (api.http 34).
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationCreateRequest(
        @NotNull(message = "startTime is required")
        LocalDateTime startTime,

        @NotNull(message = "endTime is required")
        LocalDateTime endTime) {
}
