package com.dsu.roomapi;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank String refreshToken
) { }

