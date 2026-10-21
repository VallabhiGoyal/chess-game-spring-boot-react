package com.chess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MoveRequest(
        @NotBlank @Pattern(regexp = "^[a-h][1-8]$", message = "must be a valid square") String from,
        @NotBlank @Pattern(regexp = "^[a-h][1-8]$", message = "must be a valis square") String to,
        @Pattern(regexp = "^[qrbn]?$", message = "must be q, r, b or n") String promotion) {
}
