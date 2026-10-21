package com.chess.service;

import com.chess.dto.GameResponse;
import com.chess.dto.MoveDto;
import com.chess.dto.MoveRequest;
import com.chess.engine.Engine;
import com.chess.engine.Move;
import com.chess.engine.Position;
import com.chess.entity.Game;
import com.chess.entity.GameMove;
import com.chess.entity.GameStatus;
import com.chess.exception.ApiException;
import com.chess.repository.GameRepository;
import com.chess.repository.MoveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-side authority for game state. The client never sends a FEN or a result. */
@Service
public class GameService {

    private final GameRepository games;
    private final MoveRepository moves;

    public GameService(GameRepository games, MoveRepository moves) {
        this.games = games;
        this.moves = moves;
    }

    @Transactional
    public GameResponse createGame() {
        String fen = Position.start().toFen();
        Game game = new Game();
        game.setInitialFen(fen);
        game.setCurrentFen(fen);
        game = games.save(game);
        return toResponse(game, List.of());
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(Long id) {
        return toResponse(find(id), moves.findByGameIdOrderByMoveNumberAsc(id));
    }

    @Transactional(readOnly = true)
    public List<MoveDto> getMoves(Long id) {
        find(id);
        return moves.findByGameIdOrderByMoveNumberAsc(id).stream().map(this::toDto).toList();
    }

    @Transactional
    public GameResponse makeMove(Long id, MoveRequest req) {
        Game game = find(id);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.CONFLICT, "Game is already over");
        }

        Position pos = Position.fromFen(game.getCurrentFen());
        int from = Move.square(req.from());
        int to = Move.square(req.to());
        char piece = pos.sq[from];

        if (piece == ' ') {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "No piece on " + req.from());
        }
        if (Character.isUpperCase(piece) != pos.whiteToMove) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "It is " + (pos.whiteToMove ? "White" : "Black") + "'s turn");
        }

        char promo = req.promotion() == null || req.promotion().isEmpty() ? 0 : req.promotion().charAt(0);
        Move move = new Move(from, to, promo);
        if (!Engine.legalMoves(pos).contains(move)) {
            boolean needsPromo = Character.toLowerCase(piece) == 'p' && (to / 8 == 0 || to / 8 == 7) && promo == 0;
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Illegal move: " + req.from() + req.to() + (needsPromo ? " (promotion piece required)" : ""));
        }

        String san = Engine.toSan(pos, move);
        Position after = Engine.apply(pos, move);

        List<GameMove> history = new ArrayList<>(moves.findByGameIdOrderByMoveNumberAsc(id));
        GameMove record = new GameMove();
        record.setGame(game);
        record.setMoveNumber(history.size() + 1);
        record.setFromSquare(req.from());
        record.setToSquare(req.to());
        record.setSanNotation(san);
        record.setFenAfterMove(after.toFen());
        record.setPromotionPiece(promo == 0 ? null : String.valueOf(promo));
        moves.save(record);
        history.add(record);

        game.setCurrentFen(after.toFen());
        game.setCurrentTurn(after.whiteToMove ? "WHITE" : "BLACK");
        updateOutcome(game, after, !after.whiteToMove, history);
        games.save(game);
        return toResponse(game, history);
    }

    /** Takes back the most recent move (also works after a game-ending move). */
    @Transactional
    public GameResponse undo(Long id) {
        Game game = find(id);
        List<GameMove> history = new ArrayList<>(moves.findByGameIdOrderByMoveNumberAsc(id));
        if (history.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "No moves to undo");
        }
        moves.delete(history.remove(history.size() - 1));

        String fen = history.isEmpty() ? game.getInitialFen() : history.get(history.size() - 1).getFenAfterMove();
        Position pos = Position.fromFen(fen);
        game.setCurrentFen(fen);
        game.setCurrentTurn(pos.whiteToMove ? "WHITE" : "BLACK");
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setResult(null);
        games.save(game);
        return toResponse(game, history);
    }

    // ---- helpers ----

    private void updateOutcome(Game game, Position pos, boolean moverWasWhite, List<GameMove> history) {
        boolean noMoves = Engine.legalMoves(pos).isEmpty();
        if (noMoves && Engine.inCheck(pos, pos.whiteToMove)) {
            game.setStatus(GameStatus.CHECKMATE);
            game.setResult(moverWasWhite ? "WHITE_WINS" : "BLACK_WINS");
        } else if (noMoves) {
            game.setStatus(GameStatus.STALEMATE);
            game.setResult("DRAW");
        } else if (Engine.insufficientMaterial(pos) || pos.halfmove >= 100 || repetitions(game, pos, history) >= 3) {
            game.setStatus(GameStatus.DRAW);
            game.setResult("DRAW");
        }
    }

    private long repetitions(Game game, Position pos, List<GameMove> history) {
        String key = pos.repetitionKey();
        long count = Position.fromFen(game.getInitialFen()).repetitionKey().equals(key) ? 1 : 0;
        for (GameMove m : history) {
            if (Position.fromFen(m.getFenAfterMove()).repetitionKey().equals(key)) count++;
        }
        return count;
    }

    private Game find(Long id) {
        return games.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Game " + id + " not found"));
    }

    private MoveDto toDto(GameMove m) {
        return new MoveDto(m.getMoveNumber(), m.getFromSquare(), m.getToSquare(), m.getSanNotation(), m.getPromotionPiece());
    }

    private GameResponse toResponse(Game game, List<GameMove> history) {
        Position pos = Position.fromFen(game.getCurrentFen());
        Map<String, List<String>> legal = new LinkedHashMap<>();
        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            for (Move m : Engine.legalMoves(pos)) {
                List<String> targets = legal.computeIfAbsent(Move.name(m.from()), k -> new ArrayList<>());
                String to = Move.name(m.to());
                if (!targets.contains(to)) targets.add(to);
            }
        }
        MoveDto last = history.isEmpty() ? null : toDto(history.get(history.size() - 1));
        return new GameResponse(
                game.getId(),
                game.getCurrentFen(),
                game.getCurrentTurn(),
                game.getStatus().name(),
                game.getResult(),
                Engine.inCheck(pos, pos.whiteToMove),
                last,
                history.stream().map(GameMove::getSanNotation).toList(),
                legal);
    }
}
