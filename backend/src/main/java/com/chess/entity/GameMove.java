package com.chess.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "moves", indexes = @Index(name = "idx_moves_game", columnList = "game_id, moveNumber"))
public class GameMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    /** 1-based ply count: 1 = White's first move, 2 = Black's first move. */
    @Column(nullable = false)
    private int moveNumber;

    @Column(nullable = false, length = 2)
    private String fromSquare;

    @Column(nullable = false, length = 2)
    private String toSquare;

    @Column(nullable = false, length = 12)
    private String sanNotation;

    @Column(nullable = false, length = 100)
    private String fenAfterMove;

    /** q, r, b, n or null. */
    @Column(length = 1)
    private String promotionPiece;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public int getMoveNumber() { return moveNumber; }
    public void setMoveNumber(int moveNumber) { this.moveNumber = moveNumber; }
    public String getFromSquare() { return fromSquare; }
    public void setFromSquare(String fromSquare) { this.fromSquare = fromSquare; }
    public String getToSquare() { return toSquare; }
    public void setToSquare(String toSquare) { this.toSquare = toSquare; }
    public String getSanNotation() { return sanNotation; }
    public void setSanNotation(String sanNotation) { this.sanNotation = sanNotation; }
    public String getFenAfterMove() { return fenAfterMove; }
    public void setFenAfterMove(String fenAfterMove) { this.fenAfterMove = fenAfterMove; }
    public String getPromotionPiece() { return promotionPiece; }
    public void setPromotionPiece(String promotionPiece) { this.promotionPiece = promotionPiece; }
    public Instant getCreatedAt() { return createdAt; }
}
