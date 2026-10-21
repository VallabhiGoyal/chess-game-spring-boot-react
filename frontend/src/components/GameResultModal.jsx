export default function GameResultModal({ message, onNew, onClose }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-label="Game over">
      <div className="modal">
        <h2>Game over</h2>
        <p>{message}</p>
        <div className="modal-actions">
          <button className="btn primary" onClick={onNew}>Play again</button>
          <button className="btn" onClick={onClose}>Review board</button>
        </div>
      </div>
    </div>
  );
}
