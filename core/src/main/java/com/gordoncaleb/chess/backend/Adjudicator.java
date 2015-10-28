package com.gordoncaleb.chess.backend;

import java.util.*;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Adjudicator {
    private static final Logger logger = LoggerFactory.getLogger(Adjudicator.class);

    private List<Long> validMoves;
    private Stack<Long> undoneMoves;
    private Board board;

    public Adjudicator(Board board) {
        undoneMoves = new Stack<>();
        this.board = board;
    }

    public void newGame(Board board) {
        undoneMoves = new Stack<>();
        validMoves = new ArrayList<>();
        this.board = board;
    }

    public boolean move(long move) {

        if (undoneMoves.size() > 0) {
            if (Move.equals(undoneMoves.peek(), move)) {
                undoneMoves.pop();
            } else {
                undoneMoves.clear();
            }
        }

        long matchingMove = getMatchingMove(move);

        if (matchingMove != 0) {
            if (board.makeMove(matchingMove)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public long undo() {
        long lastMove = 0;

        if (canUndo()) {
            lastMove = board.undoMove();
            undoneMoves.push(lastMove);
        }

        return lastMove;

    }

    public boolean canUndo() {
        return board.canUndo();
    }

    public long getLastUndoneMove() {

        if (hasUndoneMoves()) {
            return undoneMoves.peek();
            // board.makeMove(lastMoveUndone);
        } else {
            return 0;
        }

    }

    public boolean hasUndoneMoves() {
        return (undoneMoves.size() > 0);
    }

    public List<Long> getValidMoves() {
        board.makeNullMove();
        validMoves = board.generateValidMoves();

        return validMoves;
    }

    public Deque<Move> getMoveHistory() {
        return board.getMoveHistory();
    }

    public int getTurn() {
        return board.getTurn();
    }

    public int getPieceID(int row, int col) {
        return board.getPieceID(row, col);
    }

    public int getPiecePlayer(int row, int col) {
        Piece p = board.getPiece(row,col);
        return p.getSide();
    }

    public Piece getPiece(int row, int col) {
        return board.getPiece(row, col);
    }

    public Long getLastMoveMade() {
        return board.getLastMoveMade();
    }

    private long getMatchingMove(long move) {

        Optional<Long> matchingMove = validMoves.stream()
                .filter(m -> Move.equals(m, move)).findFirst();

        if (!matchingMove.isPresent()) {
            logger.debug("ERROR: Adjudicator says " + (new Move(move)) + " move is invalid");
        }

        return matchingMove.orElse(0L);
    }

    public Deque<Piece> getPiecesTaken(int player) {
        return board.getPiecesTakenFor(player);
    }

    public boolean placePiece(Piece piece, int toRow, int toCol) {
        return board.placePiece(piece, toRow, toCol);
    }

    public Game.GameStatus getGameStatus() {

        return board.getBoardStatus();

    }

    public boolean isGameOver() {
        return board.isGameOver();
    }

    public int getWinner() {
        if (board.isGameOver()) {
            return Side.otherSide(board.getTurn());
        } else {
            return Side.NONE;
        }
    }

    public Board getBoard() {
        return board;
    }

}
