package com.gordoncaleb.chess.ui.server;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.ui.server.Dtos.GameStateDto;
import com.gordoncaleb.chess.ui.server.Dtos.MoveDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

/**
 * In-memory registry of games plus all the rules glue between the raw
 * {@link Board} engine and the JSON the web UI speaks. Human moves are matched
 * against the board's own generated legal moves, so illegal input is rejected
 * and move metadata (captures, castling, en-passant) is filled in correctly.
 */
public class GameService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    private final ConcurrentHashMap<String, GameSession> games = new ConcurrentHashMap<>();

    public GameStateDto newGame(String humanSideName, Integer thinkTimeMs) {
        int humanSide = "BLACK".equalsIgnoreCase(humanSideName) ? Side.BLACK : Side.WHITE;
        int think = (thinkTimeMs == null || thinkTimeMs <= 0) ? 2000 : thinkTimeMs;

        String id = UUID.randomUUID().toString();
        GameSession session = new GameSession(id, BoardFactory.getStandardChessBoard(), humanSide, think);
        games.put(id, session);

        LOGGER.info("New game {} (human={}, think={}ms)", id, Side.toString(humanSide), think);
        return toState(session);
    }

    public Optional<GameSession> get(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public GameStateDto state(GameSession session) {
        return toState(session);
    }

    /** Apply a validated human move. Returns empty if the move is illegal. */
    public synchronized Optional<GameStateDto> applyHumanMove(GameSession session, Dtos.MoveRequest req) {
        Board board = session.getBoard();

        if (board.getTurn() != session.getHumanSide() || isGameOver(board)) {
            return Optional.empty();
        }

        Integer promoId = promotionId(req.promotionPiece());
        Optional<Move> match = matchMove(board,
                req.fromRow(), req.fromCol(), req.toRow(), req.toCol(), promoId);

        if (match.isEmpty()) {
            LOGGER.debug("Rejected illegal move {}", req);
            return Optional.empty();
        }

        board.makeMove(match.get());
        return Optional.of(toState(session));
    }

    /** Run the engine for its configured think time and play its choice. */
    public synchronized Optional<GameStateDto> applyEngineMove(GameSession session)
            throws ExecutionException, InterruptedException {
        Board board = session.getBoard();

        if (board.getTurn() == session.getHumanSide() || isGameOver(board)) {
            return Optional.empty();
        }

        List<Move> principalVariation =
                session.getEngine().search(board.copy(), session.getThinkTimeMs()).get();

        if (principalVariation.isEmpty()) {
            return Optional.empty();
        }

        Move engineMove = principalVariation.get(0);
        Integer promoId = engineMove.hasPromotion() ? engineMove.promotionChoice() : null;
        Optional<Move> match = matchMove(board,
                engineMove.getFromRow(), engineMove.getFromCol(),
                engineMove.getToRow(), engineMove.getToCol(), promoId);

        match.ifPresent(board::makeMove);
        return Optional.of(toState(session));
    }

    // --- rules helpers -----------------------------------------------------

    private static List<Move> legalMoves(Board board) {
        board.makeNullMove();
        return board.generateValidMoves().toList();
    }

    private static Optional<Move> matchMove(Board board,
                                            int fromRow, int fromCol,
                                            int toRow, int toCol,
                                            Integer promoId) {
        return legalMoves(board).stream()
                .filter(m -> m.getFromRow() == fromRow && m.getFromCol() == fromCol
                        && m.getToRow() == toRow && m.getToCol() == toCol)
                .filter(m -> !m.hasPromotion()
                        || promoId == null
                        || m.promotionChoice() == promoId)
                .findFirst();
    }

    private static boolean isGameOver(Board board) {
        List<Move> legal = legalMoves(board);
        return legal.isEmpty() || board.isDraw();
    }

    // --- serialization -----------------------------------------------------

    private static GameStateDto toState(GameSession session) {
        Board board = session.getBoard();
        List<Move> legal = legalMoves(board);
        boolean inCheck = board.isInCheck();

        String status;
        String winner = null;
        if (legal.isEmpty()) {
            if (inCheck) {
                status = "CHECKMATE";
                winner = Side.toString(Side.otherSide(board.getTurn()));
            } else {
                status = "STALEMATE";
            }
        } else if (board.isDraw()) {
            status = "DRAW";
        } else if (inCheck) {
            status = "CHECK";
        } else {
            status = "IN_PLAY";
        }

        boolean gameOver = legal.isEmpty() || board.isDraw();
        boolean engineToMove = !gameOver && board.getTurn() != session.getHumanSide();

        List<MoveDto> legalDtos = new ArrayList<>(legal.size());
        for (Move m : legal) {
            boolean promo = m.hasPromotion();
            legalDtos.add(new MoveDto(
                    m.getFromRow(), m.getFromCol(), m.getToRow(), m.getToCol(),
                    promo, promo ? pieceLetter(m.promotionChoice()) : null));
        }

        return new GameStateDto(
                session.getId(),
                pieceGrid(board),
                Side.toString(board.getTurn()),
                Side.toString(session.getHumanSide()),
                status,
                inCheck,
                winner,
                lastMove(board),
                board.getMoveNumber(),
                engineToMove,
                legalDtos);
    }

    private static String[][] pieceGrid(Board board) {
        String[][] grid = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null || piece.getPieceID() == NO_PIECE) {
                    grid[row][col] = null;
                } else {
                    String side = piece.getSide() == Side.WHITE ? "w" : "b";
                    grid[row][col] = side + pieceLetter(piece.getPieceID());
                }
            }
        }
        return grid;
    }

    private static int[] lastMove(Board board) {
        Move last = board.getLastMoveMade();
        if (last == null || last == Move.EMPTY_MOVE || board.getMoveNumber() == 0) {
            return null;
        }
        return new int[]{last.getFromRow(), last.getFromCol(), last.getToRow(), last.getToCol()};
    }

    private static String pieceLetter(int pieceId) {
        return switch (pieceId) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
            default -> "?";
        };
    }

    private static Integer promotionId(String letter) {
        if (letter == null) {
            return null;
        }
        return switch (letter.toUpperCase()) {
            case "Q" -> QUEEN;
            case "R" -> ROOK;
            case "B" -> BISHOP;
            case "N" -> KNIGHT;
            default -> null;
        };
    }
}
