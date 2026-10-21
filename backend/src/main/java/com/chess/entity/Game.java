package com.chess.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String initialFen;

    @Column(nullable = false, length = 100)
    private String currentFen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status = GameStatus.IN_PROGRESS;

    /** WHITE or BLACK. */
    @Column(nullable = false, length = 5)
    private String currentTurn = "WHITE";

    /** WHITE_WINS, BLACK_WINS, DRAW, or null while in progress. */
    @Column(length = 20)
    private String result;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getInitialFen() { return initialFen; }
    public void setInitialFen(String initialFen) { this.initialFen = initialFen; }
    public String getCurrentFen() { return currentFen; }
    public void setCurrentFen(String currentFen) { this.currentFen = currentFen; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
