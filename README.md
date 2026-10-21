# Chess

Spring Boot + React chess game. All chess rules are implemented in this project (no chess libraries):
`backend/src/main/java/com/chess/engine/` holds piece movement, check, castling, en passant, promotion,
checkmate, stalemate, draws and SAN notation. The React app only draws the board; the server decides what is legal.

## Run the backend (needs Java 17+, Maven, MySQL)
Windows PowerShell:
```powershell
cd backend
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
mvn spring-boot:run
```

The API runs on http://localhost:8080. Optional variables: `DB_URL`, `SERVER_PORT`, `CORS_ORIGINS`.
The `chess_db` database is created automatically and Hibernate creates the tables.

## Run the frontend (needs Node 18+)
```bash
cd frontend
npm install
npm run dev                          # http://localhost:5173
```

## Tests
```bash
cd backend && mvn test
```
The engine tests use perft (counting every reachable position to a fixed depth) on the standard reference
positions, which exercise castling, en passant and promotion.

## API
| Method | Path | Purpose |
|---|---|---|
| POST | /api/games | new game |
| GET | /api/games/{id} | current state, including legal moves |
| GET | /api/games/{id}/moves | move list |
| POST | /api/games/{id}/moves | `{"from":"e2","to":"e4","promotion":null}` |
| POST | /api/games/{id}/undo | take back the last move |
