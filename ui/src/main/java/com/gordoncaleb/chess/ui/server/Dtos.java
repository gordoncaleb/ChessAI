package com.gordoncaleb.chess.ui.server;

import java.util.List;

/**
 * Data-transfer records exchanged with the TypeScript web UI as JSON.
 */
public final class Dtos {

    private Dtos() {
    }

    /** A single legal move offered to the client. */
    public record MoveDto(int fromRow,
                          int fromCol,
                          int toRow,
                          int toCol,
                          boolean promotion,
                          String promotionPiece) {
    }

    /** Full snapshot of a game, enough for the UI to render and validate input. */
    public record GameStateDto(String gameId,
                               String[][] pieces,
                               String turn,
                               String humanSide,
                               String status,
                               boolean inCheck,
                               String winner,
                               int[] lastMove,
                               int moveNumber,
                               boolean engineToMove,
                               List<MoveDto> legalMoves) {
    }

    /** Body of POST /api/games. */
    public record NewGameRequest(String humanSide, Integer thinkTimeMs) {
    }

    /** Body of POST /api/games/{id}/moves. */
    public record MoveRequest(int fromRow,
                              int fromCol,
                              int toRow,
                              int toCol,
                              String promotionPiece) {
    }
}
