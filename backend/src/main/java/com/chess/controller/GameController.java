package com.chess.controller;

import com.chess.dto.GameResponse;
import com.chess.dto.MoveDto;
import com.chess.dto.MoveRequest;
import com.chess.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse create() {
        return service.createGame();
    }

    @GetMapping("/{id}")
    public GameResponse get(@PathVariable Long id) {
        return service.getGame(id);
    }

    @GetMapping("/{id}/moves")
    public List<MoveDto> moves(@PathVariable Long id) {
        return service.getMoves(id);
    }

    @PostMapping("/{id}/moves")
    public GameResponse move(@PathVariable Long id, @Valid @RequestBody MoveRequest request) {
        return service.makeMove(id, request);
    }

    @PostMapping("/{id}/undo")
    public GameResponse undo(@PathVariable Long id) {
        return service.undo(id);
    }
}
