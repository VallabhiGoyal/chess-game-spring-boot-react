package com.chess.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    private static long perft(Position p, int depth) {
        if (depth == 0) return 1;
        long n = 0;
        for (Move m : Engine.legalMoves(p)) n += perft(Engine.apply(p, m), depth - 1);
        return n;
    }

    private static Move mv(String from, String to) {
        return new Move(Move.square(from), Move.square(to));
    }

    @Test
    void perftStartingPosition() {
        assertEquals(20, perft(Position.start(), 1));
        assertEquals(400, perft(Position.start(), 2));
        assertEquals(8902, perft(Position.start(), 3));
        assertEquals(197281, perft(Position.start(), 4));
    }

    @Test
    void perftCastlingAndEnPassantPositions() {
        Position kiwipete = Position.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        assertEquals(2039, perft(kiwipete, 2));
        assertEquals(97862, perft(kiwipete, 3));
        assertEquals(43238, perft(Position.fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"), 4));
    }

    @Test
    void perftPromotionPositions() {
        assertEquals(9467, perft(Position.fromFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"), 3));
        assertEquals(62379, perft(Position.fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"), 3));
    }

    @Test
    void detectsFoolsMate() {
        Position p = Position.fromFen("rnb1kbnr/pppp1ppp/8/4p3/5PPq/8/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        assertTrue(Engine.inCheck(p, true));
        assertTrue(Engine.legalMoves(p).isEmpty());
    }

    @Test
    void detectsStalemate() {
        Position p = Position.fromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1");
        assertFalse(Engine.inCheck(p, false));
        assertTrue(Engine.legalMoves(p).isEmpty());
    }

    @Test
    void castlingNotAllowedThroughCheck() {
        Position p = Position.fromFen("4k3/8/8/8/8/8/5r2/R3K2R w KQ - 0 1");
        assertFalse(Engine.legalMoves(p).contains(mv("e1", "g1")));
        assertTrue(Engine.legalMoves(p).contains(mv("e1", "c1")));
    }

    @Test
    void enPassantIsLegalOnlyImmediately() {
        Position p = Position.fromFen("rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 3");
        assertTrue(Engine.legalMoves(p).contains(mv("d4", "e3")));
        Position after = Engine.apply(p, mv("d4", "e3"));
        assertEquals(' ', after.sq[Move.square("e4")]);
    }

    @Test
    void promotionOffersFourPieces() {
        Position p = Position.fromFen("8/P6k/8/8/8/8/8/K7 w - - 0 1");
        long promos = Engine.legalMoves(p).stream().filter(m -> m.promo() != 0).count();
        assertEquals(4, promos);
    }

    @Test
    void sanNotation() {
        assertEquals("e4", Engine.toSan(Position.start(), mv("e2", "e4")));
        assertEquals("Nf3", Engine.toSan(Position.start(), mv("g1", "f3")));
        Position p = Position.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        assertEquals("O-O", Engine.toSan(p, mv("e1", "g1")));
        assertEquals("O-O-O", Engine.toSan(p, mv("e1", "c1")));
        Position amb = Position.fromFen("4k3/8/8/8/8/8/K7/R6R w - - 0 1");
        assertEquals("Rad1", Engine.toSan(amb, mv("a1", "d1")));
    }

    @Test
    void insufficientMaterial() {
        assertTrue(Engine.insufficientMaterial(Position.fromFen("8/8/4k3/8/8/3K4/8/8 w - - 0 1")));
        assertTrue(Engine.insufficientMaterial(Position.fromFen("8/8/4k3/8/8/3KN3/8/8 w - - 0 1")));
        assertFalse(Engine.insufficientMaterial(Position.fromFen("8/8/4k3/8/8/3KP3/8/8 w - - 0 1")));
    }
}
