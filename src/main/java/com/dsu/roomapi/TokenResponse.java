package com.dsu.roomapi;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken
) {
    public TokenResponse(String accessToken, String tokenType, long expiresIn) {
        this(accessToken, tokenType, expiresIn, null);
    }
}

