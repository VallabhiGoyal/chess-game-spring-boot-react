package com.chess.engine;

public record Move(int from, int to, char promo) {

    public Move(int from, int to) {
        this(from, to, (char) 0);
    }

    public static int square(String name) {
        return (name.charAt(1) - '1') * 8 + (name.charAt(0) - 'a');
    }

    public static String name(int sq) {
        return "" + (char) ('a' + sq % 8) + (char) ('1' + sq / 8);
    }
}
