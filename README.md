# ChessAI

A chess engine (bitboard move generation + alpha-beta search with a transposition
table) written in Java, with a TypeScript web UI for playing against it.

## Modules

| Module      | What it is                                                              |
|-------------|-------------------------------------------------------------------------|
| `core`      | The chess engine: board, move generation, search, scoring.              |
| `benchmark` | JMH performance tests for the engine.                                   |
| `ui`        | A [Javalin](https://javalin.io) HTTP server exposing the engine as a REST API and serving the web app. |
| `web`       | The React + TypeScript front end (Vite).                                |

Requires **JDK 25** and, for the front end, **Node 18+**.

## Running it

The backend serves the built front end from a single jar, but during development
it's easiest to run the two halves separately with hot reloading.

### Development

```bash
# 1. Backend API on :7070
mvn -pl ui -am compile
mvn -pl ui exec:java

# 2. Front end on :5173 (proxies /api to :7070) — in another terminal
cd web
npm install
npm run dev
```

Open http://localhost:5173.

### Production / single jar

```bash
# Build the front end into the ui module's resources...
cd web && npm install && npm run build && cd ..
# ...then build the fat jar and run it.
mvn clean package
java -jar ui/target/ohword-ui-0.1-SNAPSHOT.jar        # serves everything on :7070
```

Open http://localhost:7070.

## HTTP API

All JSON. A `GameState` describes the board, whose turn it is, the legal moves,
and the game status.

| Method & path                     | Purpose                              |
|-----------------------------------|--------------------------------------|
| `POST /api/games`                 | New game. Body: `{humanSide, thinkTimeMs}`. |
| `GET  /api/games/{id}`            | Current state.                       |
| `POST /api/games/{id}/moves`      | Play a human move. Body: `{fromRow, fromCol, toRow, toCol, promotionPiece?}`. |
| `POST /api/games/{id}/engine-move`| Let the engine search and reply.     |

Rows are 0–7 from the top (rank 8) to the bottom (rank 1); columns are 0–7 from
the a-file to the h-file.
