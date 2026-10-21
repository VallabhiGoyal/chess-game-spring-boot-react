import { useEffect, useRef } from "react";

export default function MoveHistory({ moves }) {
  const endRef = useRef(null);
  useEffect(() => {
    endRef.current?.scrollIntoView({ block: "nearest" });
  }, [moves.length]);

  const rows = [];
  for (let i = 0; i < moves.length; i += 2) {
    rows.push({ n: i / 2 + 1, white: moves[i], black: moves[i + 1] });
  }

  return (
    <section className="panel history">
      <h2>Moves</h2>
      {rows.length === 0 ? (
        <p className="muted">Make a move to start the record.</p>
      ) : (
        <ol className="move-list">
          {rows.map((r) => (
            <li key={r.n}>
              <span className="move-no">{r.n}.</span>
              <span>{r.white}</span>
              <span>{r.black || ""}</span>
            </li>
          ))}
          <div ref={endRef} />
        </ol>
      )}
    </section>
  );
}
