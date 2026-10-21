export default function GameInfo({ status, turn, moveNumber, lastMove, captured }) {
  return (
    <section className="panel info" aria-live="polite">
      <div className="turn-row">
        <span className={`turn-chip ${turn === "w" ? "is-white" : "is-black"}`} aria-hidden="true" />
        <div>
          <h2 className={`status status-${status.label.toLowerCase().replace(/\s/g, "-")}`}>{status.label}</h2>
          <p className="muted">{status.message}</p>
        </div>
      </div>
      <dl className="facts">
        <div><dt>Move</dt><dd>{moveNumber}</dd></div>
        <div><dt>Last</dt><dd>{lastMove || "none yet"}</dd></div>
      </dl>
      <div className="captured">
        <p><span className="muted">White took</span> {captured.w || "nothing yet"}</p>
        <p><span className="muted">Black took</span> {captured.b || "nothing yet"}</p>
      </div>
    </section>
  );
}
