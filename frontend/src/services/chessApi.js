import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 10000,
});

const unwrap = (p) => p.then((r) => r.data);

export const createGame = () => unwrap(api.post("/games"));
export const getGame = (id) => unwrap(api.get(`/games/${id}`));
export const getMoves = (id) => unwrap(api.get(`/games/${id}/moves`));
export const makeMove = (id, from, to, promotion = null) =>
  unwrap(api.post(`/games/${id}/moves`, { from, to, promotion }));
export const undoMove = (id) => unwrap(api.post(`/games/${id}/undo`));

export function errorMessage(err) {
  if (err?.response?.data?.message) return err.response.data.message;
  if (err?.code === "ERR_NETWORK" || err?.code === "ECONNABORTED") {
    return "Can't reach the chess server. Check that the backend is running on port 8080.";
  }
  return err?.message || "Something went wrong.";
}
