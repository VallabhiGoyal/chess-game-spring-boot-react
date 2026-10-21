import { Routes, Route } from "react-router-dom";
import ChessGame from "./pages/ChessGame.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<ChessGame />} />
    </Routes>
  );
}
