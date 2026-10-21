package com.chess.dto;

public record MoveDto(int moveNumber, String from, String to, String san, String promotion) {
}
