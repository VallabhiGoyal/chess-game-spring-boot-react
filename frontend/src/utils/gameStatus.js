// Helpers that turn the server's game state into display data. No chess rules live here.

export function describeGame(g) {
  const turn = g.currentTurn === "WHITE" ? "White" : "Black";
  switch (g.status) {
    case "CHECKMATE":
      return { over: true, label: "Checkmate", message: `${g.result === "WHITE_WINS" ? "White" : "Black"} wins by checkmate` };
    case "STALEMATE":
      return { over: true, label: "Stalemate", message: "Draw by stalemate" };
    case "DRAW":
      return { over: true, label: "Draw", message: "Draw (repetition, fifty-move rule or insufficient material)" };
    case "RESIGNED":
      return { over: true, label: "Resigned", message: `${g.result === "WHITE_WINS" ? "Black" : "White"} resigned` };
    default:
      return g.check
        ? { over: false, label: "Check", message: `${turn} is in check` }
        : { over: false, label: "In progress", message: `${turn} to move` };
  }
}

/** Reads piece placement from a FEN string: { e4: "P", ... } plus the side to move. */
export function parseFen(fen) {
  const [placement, turn] = fen.split(" ");
  const squares = {};
  placement.split("/").forEach((row, i) => {
    const rank = 8 - i;
    let file = 0;
    for (const c of row) {
      if (/\d/.test(c)) file += Number(c);
      else squares["abcdefgh"[file++] + rank] = c;
    }
  });
  return { squares, turn };
}

export function kingSquare(squares, white) {
  const king = white ? "K" : "k";
  return Object.keys(squares).find((sq) => squares[sq] === king) || null;
}

const START = { p: 8, n: 2, b: 2, r: 2, q: 1 };
const GLYPH = { p: "♟", n: "♞", b: "♝", r: "♜", q: "♛" };

/** Pieces each side has taken, worked out from what is missing on the board. */
export function capturedFromFen(fen) {
  const { squares } = parseFen(fen);
  const count = { w: {}, b: {} };
  for (const c of Object.values(squares)) {
    const side = c === c.toUpperCase() ? "w" : "b";
    const t = c.toLowerCase();
    count[side][t] = (count[side][t] || 0) + 1;
  }
  const taken = (lostSide) =>
    Object.keys(START)
      .map((t) => GLYPH[t].repeat(Math.max(0, START[t] - (count[lostSide][t] || 0))))
      .join("");
  return { w: taken("b"), b: taken("w") }; // key = side that captured
}
