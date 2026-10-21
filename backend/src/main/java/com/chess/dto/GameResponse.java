package com.chess.dto;

import java.util.List;
import java.util.Map;

public record GameResponse(
        Long gameId,
        String fen,
        String currentTurn,
        String status,
        String result,
        boolean check,
        MoveDto lastMove,
        List<String> moveHistory,
        Map<String, List<String>> legalMoves) {
}
