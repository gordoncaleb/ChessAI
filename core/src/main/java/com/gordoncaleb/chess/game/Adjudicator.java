package com.gordoncaleb.chess.game;

import java.util.*;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Adjudicator {
    private static final Logger logger = LoggerFactory.getLogger(Adjudicator.class);

    private List<Move> validMoves;
    private Stack<Move> undoneMoves;
    private Board board;
    private Game.GameStatus gameStatus;

    public Adjudicator(Board board) {
        undoneMoves = new Stack<>();
        this.board = board;
    }

    public void newGame(Board board) {
        undoneMoves = new Stack<>();
        validMoves = new ArrayList<>();
        this.board = board;
    }

    public boolean move(Move move) {

        if (undoneMoves.size() > 0) {
            if (Move.equals(undoneMoves.peek(), move)) {
                undoneMoves.pop();
            } else {
                undoneMoves.clear();
            }
        }

        Move matchingMove = getMatchingMove(move);

        if (matchingMove != null) {
            if (board.makeMove(matchingMove)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public Move undo() {
        Move lastMove = null;

        if (canUndo()) {
            lastMove = board.undoMove();
            undoneMoves.push(lastMove);
        }

        return lastMove;

    }

    public boolean canUndo() {
        return board.canUndo();
    }

    public Move getLastUndoneMove() {

        if (hasUndoneMoves()) {
            return undoneMoves.peek();
            // board.makeMove(lastMoveUndone);
        } else {
            return null;
        }

    }

    public boolean hasUndoneMoves() {
        return (undoneMoves.size() > 0);
    }

    public List<Move> getValidMoves() {
        board.makeNullMove();
        validMoves = board.generateValidMoves().toList();

        return validMoves;
    }

    public MoveContainer getMoveHistory() {
        return board.getMoveHistory();
    }

    public int getTurn() {
        return board.getTurn();
    }

    public int getPieceID(int row, int col) {
        return board.getPieceID(row, col);
    }

    public int getPiecePlayer(int row, int col) {
        Piece p = board.getPiece(row, col);
        return p.getSide();
    }

    public Piece getPiece(int row, int col) {
        return board.getPiece(row, col);
    }

    public Move getLastMoveMade() {
        return board.getLastMoveMade();
    }

    private Move getMatchingMove(Move move) {

        Optional<Move> matchingMove = validMoves.stream()
                .filter(m -> Move.equals(m, move)).findFirst();

        if (!matchingMove.isPresent()) {
            logger.debug("ERROR: Adjudicator says " + move.toString() + " move is invalid");
        }

        return matchingMove.orElse(null);
    }

    public Deque<Piece> getPiecesTaken(int player) {
        return board.getPiecesTakenFor(player);
    }

    public boolean placePiece(Piece piece, int toRow, int toCol) {
        return board.placePiece(piece, toRow, toCol);
    }

    public Game.GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Game.GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public boolean isGameOver() {
        return board.isGameOver();
    }

    public int getWinner() {
        if (board.isGameOver()) {
            return Side.otherSide(board.getTurn());
        } else {
            return Side.NEITHER;
        }
    }

    public Board getBoard() {
        return board;
    }

}
