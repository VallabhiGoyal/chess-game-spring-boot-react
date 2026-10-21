import { useCallback, useEffect, useState } from "react";
import ChessBoard from "../components/ChessBoard.jsx";
import GameInfo from "../components/GameInfo.jsx";
import MoveHistory from "../components/MoveHistory.jsx";
import GameControls from "../components/GameControls.jsx";
import GameResultModal from "../components/GameResultModal.jsx";
import { createGame, getGame, makeMove, undoMove, errorMessage } from "../services/chessApi.js";
import { capturedFromFen, describeGame } from "../utils/gameStatus.js";

export default function ChessGame() {
  const [game, setGame] = useState(null);
  const [orientation, setOrientation] = useState("white");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [loadFailed, setLoadFailed] = useState(false);
  const [modalOpen, setModalOpen] = useState(true);
  const [boardKey, setBoardKey] = useState(0); // remounting the board discards any piece the server rejected

  const startGame = useCallback(async () => {
    setBusy(true);
    setError("");
    try {
      const g = await createGame();
      localStorage.setItem("chessGameId", String(g.gameId));
      setGame(g);
      setModalOpen(true);
      setLoadFailed(false);
      setBoardKey((k) => k + 1);
    } catch (e) {
      setError(errorMessage(e));
      setLoadFailed(true);
    } finally {
      setBusy(false);
    }
  }, []);

  // Resume the last game after a refresh, otherwise start a new one.
  useEffect(() => {
    (async () => {
      const saved = localStorage.getItem("chessGameId");
      if (saved) {
        try {
          setGame(await getGame(saved));
          return;
        } catch (e) {
          if (e?.response?.status !== 404) {
            setError(errorMessage(e));
            setLoadFailed(true);
            return;
          }
        }
      }
      startGame();
    })();
  }, [startGame]);

  const handleMove = async (from, to, promotion) => {
    setBusy(true);
    setError("");
    try {
      setGame(await makeMove(game.gameId, from, to, promotion));
      setModalOpen(true);
    } catch (e) {
      setError(errorMessage(e));
      try { setGame(await getGame(game.gameId)); } catch { /* keep the current state */ }
      setBoardKey((k) => k + 1);
    } finally {
      setBusy(false);
    }
  };

  const handleUndo = async () => {
    setBusy(true);
    setError("");
    try {
      setGame(await undoMove(game.gameId));
      setBoardKey((k) => k + 1);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  const handleNew = () => {
    const inProgress = game && game.moveHistory.length > 0 && game.status === "IN_PROGRESS";
    if (inProgress && !window.confirm("Start a new game? The current game will be lost.")) return;
    startGame();
  };

  if (!game) {
    return (
      <main className="app">
        <header className="topbar"><h1>Chess</h1></header>
        {loadFailed ? (
          <div className="panel">
            <p className="error" role="alert">{error}</p>
            <button className="btn primary" onClick={startGame}>Try again</button>
          </div>
        ) : (
          <p className="muted">Setting up the board…</p>
        )}
      </main>
    );
  }

  const status = describeGame(game);
  const turn = game.currentTurn === "WHITE" ? "w" : "b";
  const history = game.moveHistory;

  return (
    <main className="app">
      <header className="topbar">
        <h1>Chess</h1>
        <p className="muted">Two players, one board. White moves first.</p>
      </header>

      <div className="layout">
        <div className="board-col">
          <PlayerTag name="Black" active={turn === "b" && !status.over} />
          <ChessBoard
            key={boardKey}
            game={game}
            orientation={orientation}
            disabled={status.over || busy}
            onMove={handleMove}
          />
          <PlayerTag name="White" active={turn === "w" && !status.over} />
          {error && <p className="error" role="alert">{error}</p>}
        </div>

        <aside className="side-col">
          <GameInfo
            status={status}
            turn={turn}
            moveNumber={Math.floor(history.length / 2) + 1}
            lastMove={history.length ? history[history.length - 1] : ""}
            captured={capturedFromFen(game.fen)}
          />
          <MoveHistory moves={history} />
          <GameControls
            canUndo={history.length > 0 && !busy}
            onNew={handleNew}
            onUndo={handleUndo}
            onFlip={() => setOrientation((o) => (o === "white" ? "black" : "white"))}
          />
        </aside>
      </div>

      {status.over && modalOpen && (
        <GameResultModal message={status.message} onNew={startGame} onClose={() => setModalOpen(false)} />
      )}
    </main>
  );
}

function PlayerTag({ name, active }) {
  return (
    <div className={`player-tag ${active ? "active" : ""}`}>
      <span className={`dot ${name === "White" ? "w" : "b"}`} />
      {name}
    </div>
  );
}
