import { useMemo, useState } from "react";
import { Chessboard } from "react-chessboard";
import { kingSquare, parseFen } from "../utils/gameStatus.js";

const LAST_MOVE = { backgroundColor: "rgba(224, 164, 59, 0.38)" };
const SELECTED = { backgroundColor: "rgba(224, 164, 59, 0.6)" };
const CHECK = { background: "radial-gradient(circle, rgba(214,64,64,.95) 0%, rgba(214,64,64,.35) 55%, transparent 75%)" };
const dot = (capture) => ({
  background: capture
    ? "radial-gradient(circle, transparent 58%, rgba(20,24,32,.35) 60%)"
    : "radial-gradient(circle, rgba(20,24,32,.32) 22%, transparent 24%)",
});

function isPromotion(piece, to) {
  return piece?.[1] === "P" && ((piece[0] === "w" && to[1] === "8") || (piece[0] === "b" && to[1] === "1"));
}

/**
 * Renders the board. All legality comes from `game.legalMoves` (computed by the server engine);
 * `onMove(from, to, promotion)` sends the move to the server.
 */
export default function ChessBoard({ game, orientation, disabled, onMove }) {
  const [selected, setSelected] = useState(null);
  const legal = game.legalMoves || {};
  const { squares, turn } = useMemo(() => parseFen(game.fen), [game.fen]);

  const styles = useMemo(() => {
    const s = {};
    if (game.lastMove) {
      s[game.lastMove.from] = LAST_MOVE;
      s[game.lastMove.to] = LAST_MOVE;
    }
    if (game.check) {
      const k = kingSquare(squares, turn === "w");
      if (k) s[k] = { ...(s[k] || {}), ...CHECK };
    }
    if (selected && !disabled) {
      s[selected] = SELECTED;
      for (const to of legal[selected] || []) {
        s[to] = { ...(s[to] || {}), ...dot(Boolean(squares[to])) };
      }
    }
    return s;
  }, [game, squares, turn, selected, disabled, legal]);

  const handleDrop = (from, to, piece) => {
    setSelected(null);
    if (disabled || !(legal[from] || []).includes(to)) return false;
    if (isPromotion(piece, to)) return true; // react-chessboard opens its picker; handlePromotion sends the move
    onMove(from, to);
    return true;
  };

  const handlePromotion = (piece, from, to) => {
    if (!piece || !from || !to) return false;
    onMove(from, to, piece[1].toLowerCase());
    return true;
  };

  const handleSquareClick = (square) => {
    if (disabled) return;
    if (selected && (legal[selected] || []).includes(square)) {
      const movingPawn = squares[selected]?.toLowerCase() === "p";
      const promo = movingPawn && (square[1] === "8" || square[1] === "1") ? "q" : undefined; // click auto-queens; drag to pick
      const from = selected;
      setSelected(null);
      onMove(from, square, promo);
      return;
    }
    setSelected(legal[square] && selected !== square ? square : null);
  };

  return (
    <div className="board-frame">
      <Chessboard
        id="main-board"
        position={game.fen}
        boardOrientation={orientation}
        arePiecesDraggable={!disabled}
        onPieceDrop={handleDrop}
        onPromotionPieceSelect={handlePromotion}
        onSquareClick={handleSquareClick}
        customSquareStyles={styles}
        customDarkSquareStyle={{ backgroundColor: "#5b6f8c" }}
        customLightSquareStyle={{ backgroundColor: "#e6e0d2" }}
        customBoardStyle={{ borderRadius: "6px" }}
        animationDuration={160}
      />
    </div>
  );
}
