package com.dsu.roomapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank
    @Pattern(regexp = "[a-z0-9]{3,20}", message = "username must be 3 to 20 lowercase letters or digits")
    String username,

    @NotBlank
    @Size(min = 8, max = 72, message = "password must be 8 to 72 characters")
    String password
) { }

