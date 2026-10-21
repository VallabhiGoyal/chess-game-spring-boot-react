package com.chess.engine;

import java.util.Arrays;

/** A full chess position: piece placement, side to move, castling rights, en passant square and clocks. */
public final class Position {

    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** Index = rank * 8 + file. Uppercase = White, lowercase = Black, space = empty. */
    public final char[] sq = new char[64];
    public boolean whiteToMove = true;
    public boolean whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide;
    /** En passant target square, only set when an enemy pawn can actually capture there; otherwise -1. */
    public int ep = -1;
    public int halfmove = 0;
    public int fullmove = 1;

    public static Position start() {
        return fromFen(START_FEN);
    }

    public static Position fromFen(String fen) {
        String[] f = fen.trim().split("\\s+");
        if (f.length < 4) throw new IllegalArgumentException("Invalid FEN: " + fen);
        String[] ranks = f[0].split("/");
        if (ranks.length != 8) throw new IllegalArgumentException("Invalid FEN: " + fen);

        Position p = new Position();
        Arrays.fill(p.sq, ' ');
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i;
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    if (file > 7) throw new IllegalArgumentException("Invalid FEN: " + fen);
                    p.sq[rank * 8 + file++] = c;
                }
            }
            if (file != 8) throw new IllegalArgumentException("Invalid FEN: " + fen);
        }
        p.whiteToMove = f[1].equals("w");
        p.whiteKingSide = f[2].contains("K");
        p.whiteQueenSide = f[2].contains("Q");
        p.blackKingSide = f[2].contains("k");
        p.blackQueenSide = f[2].contains("q");
        p.ep = f[3].equals("-") ? -1 : Move.square(f[3]);
        p.halfmove = f.length > 4 ? Integer.parseInt(f[4]) : 0;
        p.fullmove = f.length > 5 ? Integer.parseInt(f[5]) : 1;
        return p;
    }

    public String toFen() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                char c = sq[rank * 8 + file];
                if (c == ' ') {
                    empty++;
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0; }
                    sb.append(c);
                }
            }
            if (empty > 0) sb.append(empty);
            if (rank > 0) sb.append('/');
        }
        sb.append(whiteToMove ? " w " : " b ");
        String rights = (whiteKingSide ? "K" : "") + (whiteQueenSide ? "Q" : "")
                + (blackKingSide ? "k" : "") + (blackQueenSide ? "q" : "");
        sb.append(rights.isEmpty() ? "-" : rights).append(' ');
        sb.append(ep < 0 ? "-" : Move.name(ep)).append(' ').append(halfmove).append(' ').append(fullmove);
        return sb.toString();
    }

    /** Position identity for repetition detection: placement, side to move, castling rights, en passant. */
    public String repetitionKey() {
        String[] f = toFen().split(" ");
        return f[0] + " " + f[1] + " " + f[2] + " " + f[3];
    }

    public Position copy() {
        Position n = new Position();
        System.arraycopy(sq, 0, n.sq, 0, 64);
        n.whiteToMove = whiteToMove;
        n.whiteKingSide = whiteKingSide;
        n.whiteQueenSide = whiteQueenSide;
        n.blackKingSide = blackKingSide;
        n.blackQueenSide = blackQueenSide;
        n.ep = ep;
        n.halfmove = halfmove;
        n.fullmove = fullmove;
        return n;
    }
}
