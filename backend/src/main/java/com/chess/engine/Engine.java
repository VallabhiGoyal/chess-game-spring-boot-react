package com.chess.engine;

import java.util.ArrayList;
import java.util.List;

public final class Engine {

    private static final int[][] KNIGHT = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
    private static final int[][] KING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
    private static final int[][] ROOK_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] BISHOP_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private Engine() {
    }

    // ---------------------------------------------------------------- public API

    /** All legal moves for the side to move. */
    public static List<Move> legalMoves(Position p) {
        List<Move> legal = new ArrayList<>();
        for (Move m : pseudoLegalMoves(p)) {
            if (!inCheck(apply(p, m), p.whiteToMove)) legal.add(m);
        }
        return legal;
    }

    /** Whether the given side's king is attacked. */
    public static boolean inCheck(Position p, boolean white) {
        char king = white ? 'K' : 'k';
        for (int s = 0; s < 64; s++) {
            if (p.sq[s] == king) return attacked(p, s, !white);
        }
        return false;
    }

    /** Returns the position after playing m */
    public static Position apply(Position p, Move m) {
        Position n = p.copy();
        boolean white = p.whiteToMove;
        char piece = n.sq[m.from()];
        boolean pawn = Character.toLowerCase(piece) == 'p';
        boolean capture = n.sq[m.to()] != ' ';

        if (pawn && m.to() == p.ep) { // en passant
            n.sq[m.to() + (white ? -8 : 8)] = ' ';
            capture = true;
        }

        n.sq[m.to()] = piece;
        n.sq[m.from()] = ' ';
        if (m.promo() != 0) {
            n.sq[m.to()] = white ? Character.toUpperCase(m.promo()) : Character.toLowerCase(m.promo());
        }

        if (Character.toLowerCase(piece) == 'k' && Math.abs(m.to() - m.from()) == 2) { // castling
            if (m.to() > m.from()) {
                n.sq[m.from() + 1] = n.sq[m.from() + 3];
                n.sq[m.from() + 3] = ' ';
            } else {
                n.sq[m.from() - 1] = n.sq[m.from() - 4];
                n.sq[m.from() - 4] = ' ';
            }
        }

        // Any move from or to a king/rook home square removes the castling rights
        for (int s : new int[]{m.from(), m.to()}) {
            if (s == 4) { n.whiteKingSide = false; n.whiteQueenSide = false; }
            if (s == 60) { n.blackKingSide = false; n.blackQueenSide = false; }
            if (s == 0) n.whiteQueenSide = false;
            if (s == 7) n.whiteKingSide = false;
            if (s == 56) n.blackQueenSide = false;
            if (s == 63) n.blackKingSide = false;
        }

        n.ep = -1;
        if (pawn && Math.abs(m.to() - m.from()) == 16) {
            char enemyPawn = white ? 'p' : 'P';
            int f = m.to() % 8;
            boolean capturable = (f > 0 && n.sq[m.to() - 1] == enemyPawn) || (f < 7 && n.sq[m.to() + 1] == enemyPawn);
            if (capturable) n.ep = (m.from() + m.to()) / 2;
        }

        n.halfmove = (pawn || capture) ? 0 : p.halfmove + 1;
        if (!white) n.fullmove++;
        n.whiteToMove = !white;
        return n;
    }

    /** Standard algebraic notation for a legal move in position p, e.g. e4, Nbd7, exd5, O-O, e8=Q+. */
    public static String toSan(Position p, Move m) {
        char piece = p.sq[m.from()];
        char type = Character.toUpperCase(piece);
        StringBuilder sb = new StringBuilder();

        if (type == 'K' && Math.abs(m.to() - m.from()) == 2) {
            sb.append(m.to() > m.from() ? "O-O" : "O-O-O");
        } else {
            boolean capture = p.sq[m.to()] != ' ' || (type == 'P' && m.to() == p.ep);
            if (type == 'P') {
                if (capture) sb.append((char) ('a' + m.from() % 8)).append('x');
                sb.append(Move.name(m.to()));
                if (m.promo() != 0) sb.append('=').append(Character.toUpperCase(m.promo()));
            } else {
                sb.append(type);
                boolean sameFile = false, sameRank = false, ambiguous = false;
                for (Move o : legalMoves(p)) {
                    if (o.to() == m.to() && o.from() != m.from() && p.sq[o.from()] == piece) {
                        ambiguous = true;
                        if (o.from() % 8 == m.from() % 8) sameFile = true;
                        if (o.from() / 8 == m.from() / 8) sameRank = true;
                    }
                }
                if (ambiguous) {
                    String from = Move.name(m.from());
                    if (!sameFile) sb.append(from.charAt(0));
                    else if (!sameRank) sb.append(from.charAt(1));
                    else sb.append(from);
                }
                if (capture) sb.append('x');
                sb.append(Move.name(m.to()));
            }
        }

        Position after = apply(p, m);
        if (inCheck(after, after.whiteToMove)) {
            sb.append(legalMoves(after).isEmpty() ? '#' : '+');
        }
        return sb.toString();
    }

    /** Dead position: neither side can ever deliver checkmate. */
    public static boolean insufficientMaterial(Position p) {
        int minors = 0, knights = 0, lightBishops = 0, darkBishops = 0;
        for (int s = 0; s < 64; s++) {
            char c = Character.toLowerCase(p.sq[s]);
            if (c == ' ' || c == 'k') continue;
            if (c == 'p' || c == 'r' || c == 'q') return false;
            minors++;
            if (c == 'n') knights++;
            else if ((s / 8 + s % 8) % 2 == 0) darkBishops++;
            else lightBishops++;
        }
        if (minors <= 1) return true; // K vs K, K+minor vs K
        return knights == 0 && (lightBishops == 0 || darkBishops == 0); // only same-coloured bishops
    }

    // ---------------------------------------------------------------- move generation

    private static List<Move> pseudoLegalMoves(Position p) {
        List<Move> moves = new ArrayList<>(48);
        boolean white = p.whiteToMove;
        for (int s = 0; s < 64; s++) {
            char c = p.sq[s];
            if (!own(c, white)) continue;
            int f = s % 8, r = s / 8;
            switch (Character.toLowerCase(c)) {
                case 'p' -> pawnMoves(p, s, f, r, white, moves);
                case 'n' -> { for (int[] d : KNIGHT) step(p, s, f + d[0], r + d[1], white, moves); }
                case 'b' -> slide(p, s, BISHOP_DIRS, white, moves);
                case 'r' -> slide(p, s, ROOK_DIRS, white, moves);
                case 'q' -> { slide(p, s, ROOK_DIRS, white, moves); slide(p, s, BISHOP_DIRS, white, moves); }
                case 'k' -> {
                    for (int[] d : KING) step(p, s, f + d[0], r + d[1], white, moves);
                    castlingMoves(p, s, white, moves);
                }
                default -> { }
            }
        }
        return moves;
    }

    private static void pawnMoves(Position p, int s, int f, int r, boolean white, List<Move> moves) {
        int dir = white ? 1 : -1;
        int startRank = white ? 1 : 6;
        int lastRank = white ? 7 : 0;
        int r1 = r + dir;
        if (r1 < 0 || r1 > 7) return;

        int one = r1 * 8 + f;
        if (p.sq[one] == ' ') {
            addPawnMove(s, one, r1 == lastRank, moves);
            int two = (r + 2 * dir) * 8 + f;
            if (r == startRank && p.sq[two] == ' ') moves.add(new Move(s, two));
        }
        for (int df = -1; df <= 1; df += 2) {
            int nf = f + df;
            if (nf < 0 || nf > 7) continue;
            int to = r1 * 8 + nf;
            if (enemy(p.sq[to], white) || to == p.ep) addPawnMove(s, to, r1 == lastRank, moves);
        }
    }

    private static void addPawnMove(int from, int to, boolean promotes, List<Move> moves) {
        if (promotes) {
            for (char c : new char[]{'q', 'r', 'b', 'n'}) moves.add(new Move(from, to, c));
        } else {
            moves.add(new Move(from, to));
        }
    }

    private static void step(Position p, int from, int f, int r, boolean white, List<Move> moves) {
        if (f < 0 || f > 7 || r < 0 || r > 7) return;
        int to = r * 8 + f;
        if (!own(p.sq[to], white)) moves.add(new Move(from, to));
    }

    private static void slide(Position p, int from, int[][] dirs, boolean white, List<Move> moves) {
        for (int[] d : dirs) {
            int f = from % 8 + d[0], r = from / 8 + d[1];
            while (f >= 0 && f <= 7 && r >= 0 && r <= 7) {
                int to = r * 8 + f;
                char t = p.sq[to];
                if (t == ' ') {
                    moves.add(new Move(from, to));
                } else {
                    if (enemy(t, white)) moves.add(new Move(from, to));
                    break;
                }
                f += d[0];
                r += d[1];
            }
        }
    }

    private static void castlingMoves(Position p, int s, boolean white, List<Move> moves) {
        int home = white ? 4 : 60;
        if (s != home || attacked(p, home, !white)) return;
        char rook = white ? 'R' : 'r';
        boolean kingSide = white ? p.whiteKingSide : p.blackKingSide;
        boolean queenSide = white ? p.whiteQueenSide : p.blackQueenSide;

        if (kingSide && p.sq[home + 3] == rook && p.sq[home + 1] == ' ' && p.sq[home + 2] == ' '
                && !attacked(p, home + 1, !white) && !attacked(p, home + 2, !white)) {
            moves.add(new Move(home, home + 2));
        }
        if (queenSide && p.sq[home - 4] == rook && p.sq[home - 1] == ' ' && p.sq[home - 2] == ' '
                && p.sq[home - 3] == ' ' && !attacked(p, home - 1, !white) && !attacked(p, home - 2, !white)) {
            moves.add(new Move(home, home - 2));
        }
    }

    // ---------------------------------------------------------------- attack detection

    /** Is `target` attacked by any piece of the given colour? */
    static boolean attacked(Position p, int target, boolean byWhite) {
        int f = target % 8, r = target / 8;

        int pawnRank = byWhite ? r - 1 : r + 1;
        char pawn = byWhite ? 'P' : 'p';
        if (pawnRank >= 0 && pawnRank <= 7) {
            for (int df = -1; df <= 1; df += 2) {
                int pf = f + df;
                if (pf >= 0 && pf <= 7 && p.sq[pawnRank * 8 + pf] == pawn) return true;
            }
        }
        if (hasPieceAt(p, f, r, KNIGHT, byWhite ? 'N' : 'n')) return true;
        if (hasPieceAt(p, f, r, KING, byWhite ? 'K' : 'k')) return true;

        char rook = byWhite ? 'R' : 'r', bishop = byWhite ? 'B' : 'b', queen = byWhite ? 'Q' : 'q';
        for (int[] d : ROOK_DIRS) {
            char c = firstPiece(p, f, r, d);
            if (c == rook || c == queen) return true;
        }
        for (int[] d : BISHOP_DIRS) {
            char c = firstPiece(p, f, r, d);
            if (c == bishop || c == queen) return true;
        }
        return false;
    }

    private static boolean hasPieceAt(Position p, int f, int r, int[][] offsets, char piece) {
        for (int[] d : offsets) {
            int nf = f + d[0], nr = r + d[1];
            if (nf >= 0 && nf <= 7 && nr >= 0 && nr <= 7 && p.sq[nr * 8 + nf] == piece) return true;
        }
        return false;
    }

    private static char firstPiece(Position p, int f, int r, int[] d) {
        int nf = f + d[0], nr = r + d[1];
        while (nf >= 0 && nf <= 7 && nr >= 0 && nr <= 7) {
            char c = p.sq[nr * 8 + nf];
            if (c != ' ') return c;
            nf += d[0];
            nr += d[1];
        }
        return ' ';
    }

    private static boolean own(char c, boolean white) {
        return c != ' ' && Character.isUpperCase(c) == white;
    }

    private static boolean enemy(char c, boolean white) {
        return c != ' ' && Character.isUpperCase(c) != white;
    }
}
