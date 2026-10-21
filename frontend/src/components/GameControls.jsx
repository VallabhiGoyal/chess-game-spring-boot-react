export default function GameControls({ canUndo, onNew, onUndo, onFlip }) {
  return (
    <section className="controls">
      <button className="btn primary" onClick={onNew}>New game</button>
      <button className="btn" onClick={onUndo} disabled={!canUndo}>Undo move</button>
      <button className="btn" onClick={onFlip}>Flip board</button>
    </section>
  );
}
