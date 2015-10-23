package com.gordoncaleb.chess.backend;

import java.util.*;
import java.util.ArrayList;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.io.XMLParser;
import com.gordoncaleb.chess.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.bitboard.BitBoard.*;

public class Board {
    private static final Logger logger = LoggerFactory.getLogger(Board.class);
    private final RNGTable rngTable = RNGTable.instance;

    public static final int CASTLED_NEAR = 2;
    public static final int CASTLED_FAR = 1;
    public static final int HAS_NOT_CASTLED = 0;

    private final int[] materialRow = {0, 7};

    private Piece[][] board = new Piece[8][8];
    private Game.GameStatus boardStatus = Game.GameStatus.IN_PLAY;
    private ArrayList<Long> validMoves = new ArrayList<>(100);
    private LinkedList<Piece>[] pieces = new LinkedList[2];
    private Deque<Piece>[] piecesTaken = new ArrayDeque[2];
    private int[] castleHistory = new int[2];

    private Piece[] kings = new Piece[2];
    private int[][] rookStartCols = new int[2][2];
    private int[] kingStartCols = new int[2];

    private int turn;
    private long hashCode;
    private int hashCodeFreq;
    private Deque<Move> moveHistory = new ArrayDeque<>();
    private Deque<Long> hashCodeHistory = new ArrayDeque<>();
    private Map<Long, Integer> hashCodeFrequencies = new HashMap<>();

    private long[] nullMoveInfo = {0, ALL_ONES, 0};

    private long[][] posBitBoard = new long[PIECES_COUNT][2];
    private long[] allPosBitBoard = new long[2];

    public Board() {
        this.pieces[Side.WHITE] = new LinkedList<>();
        this.pieces[Side.BLACK] = new LinkedList<>();

        this.piecesTaken[Side.WHITE] = new ArrayDeque<>();
        this.piecesTaken[Side.BLACK] = new ArrayDeque<>();

        this.turn = Side.WHITE;
        this.nullMoveInfo = new long[3];

        kings[Side.BLACK] = new Piece(KING, Side.BLACK, -1, -1, false);
        kings[Side.WHITE] = new Piece(KING, Side.WHITE, -1, -1, false);

        placePiece(kings[Side.BLACK], 0, 0);
        placePiece(kings[Side.WHITE], 7, 0);
    }

    public Board(List<Piece>[] pieces, int turn, Deque<Move> moveHistory, int[][] rookStartCols, int[] kingCols) {
        this.pieces[Side.WHITE] = new LinkedList<>();
        this.pieces[Side.BLACK] = new LinkedList<>();

        this.piecesTaken[Side.WHITE] = new ArrayDeque<>();
        this.piecesTaken[Side.BLACK] = new ArrayDeque<>();

        this.turn = turn;
        this.nullMoveInfo = new long[3];

        long[][] posBitBoard = new long[PIECES_COUNT][2];
        // long[] pawnPosBitboard = { 0, 0 };
        // long[] kingPosBitboard = { 0, 0 };

        int[] pawnRow = new int[2];
        pawnRow[Side.BLACK] = 1;
        pawnRow[Side.WHITE] = 6;

        Piece temp;

        for (int i = 0; i < pieces.length; i++) {

            for (int p = 0; p < pieces[i].size(); p++) {
                temp = pieces[i].get(p).getCopy();

                this.pieces[i].add(temp);

                board[temp.getRow()][temp.getCol()] = temp;

                posBitBoard[temp.getPieceID()][i] |= temp.getBit();

                if (temp.getPieceID() == PAWN) {

                    if (temp.getRow() != pawnRow[i]) {
                        temp.setMoved(true);
                    }
                }

                if (temp.getPieceID() == KING) {

                    kings[i] = temp;

                    if (temp.getRow() != materialRow[i]) {
                        temp.setMoved(true);
                    }
                }
            }

        }

        this.posBitBoard = posBitBoard;

        for (int i = 0; i < PIECES_COUNT; i++) {
            this.allPosBitBoard[0] |= posBitBoard[i][0];
            this.allPosBitBoard[1] |= posBitBoard[i][1];
        }

        hashCode = generateHashCode();
        hashCodeFreq = incrementHashCodeFrequency(hashCode);

        if (moveHistory.size() > 0) {
            int moveSide;
            if (moveHistory.size() % 2 == 0) {
                moveSide = turn;
            } else {
                moveSide = Side.otherSide(turn);
            }

            for (Move m : moveHistory) {
                this.moveHistory.push(new Move(m.getMoveLong()));
                if (m.hasPieceTaken()) {
                    piecesTaken[Side.otherSide(moveSide)]
                            .push(new Piece(m.getPieceTakenID(),
                                    Side.otherSide(moveSide),
                                    m.getPieceTakenRow(),
                                    m.getPieceTakenCol(),
                                    m.getPieceTakenHasMoved())
                            );
                }

                moveSide = Side.otherSide(moveSide);
            }

        } else {
            loadPiecesTaken();
        }

        if (kingCols == null || rookStartCols == null) {
            initializeCastleSetup();
        } else {
            this.kingStartCols = kingCols;
            this.rookStartCols = rookStartCols;
        }

    }

    public boolean makeMove(final long move) {

        final int fromRow = Move.getFromRow(move);
        final int fromCol = Move.getFromCol(move);
        final int toRow = Move.getToRow(move);
        final int toCol = Move.getToCol(move);
        final Move.MoveNote note = Move.getNote(move);

        if (board[fromRow][fromCol].getSide() != turn) {
            logger.debug("Invalid move " + new Move(move).toString());
            return false;
        }

        final int nextSide = Side.otherSide(turn);
        // save off hashCode
        hashCodeHistory.push(hashCode);

        // remove previous castle options
        hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK),
                this.nearRookHasMoved(Side.BLACK),
                this.kingHasMoved(Side.BLACK),
                this.farRookHasMoved(Side.WHITE),
                this.nearRookHasMoved(Side.WHITE),
                this.kingHasMoved(Side.WHITE));

        // remove taken piece first
        if (Move.hasPieceTaken(move)) {

            final Piece pieceTaken = board[Move.getPieceTakenRow(move)][Move.getPieceTakenCol(move)];

            // remove pieceTaken from vectors
            pieces[nextSide].remove(pieceTaken);
            piecesTaken[nextSide].push(pieceTaken);

            posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] ^= pieceTaken.getBit();
            allPosBitBoard[pieceTaken.getSide()] ^= pieceTaken.getBit();

            // remove ref to piecetaken on board
            board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

            // remove old hash from piece that was taken, if any
            hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getSide(), pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());

        }

        if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_NEAR) {
                rook = board[materialRow[turn]][rookStartCols[turn][1]];

                movePiece(king, materialRow[turn], 6, Move.MoveNote.NONE);
                movePiece(rook, materialRow[turn], 5, Move.MoveNote.NONE);

                board[materialRow[turn]][6] = king;
                board[materialRow[turn]][5] = rook;

                castleHistory[turn] = CASTLED_NEAR;
            } else {
                rook = board[materialRow[turn]][rookStartCols[turn][0]];

                movePiece(king, materialRow[turn], 2, Move.MoveNote.NONE);
                movePiece(rook, materialRow[turn], 3, Move.MoveNote.NONE);

                board[materialRow[turn]][2] = king;
                board[materialRow[turn]][3] = rook;

                castleHistory[turn] = CASTLED_FAR;
            }

        } else {

            movePiece(board[fromRow][fromCol], toRow, toCol, note);

        }

        // if last move made is pawn leap, remove en passant file num
        if (getLastMoveMade() != 0) {
            if (Move.getNote(getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {
                hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
            }
        }

        // if new move is pawn leap, add en passant file num
        if (note == Move.MoveNote.PAWN_LEAP) {
            hashCode ^= rngTable.getEnPassantFile(Move.getToCol(move));
        }

        // add new castle options
        hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK),
                this.nearRookHasMoved(Side.BLACK),
                this.kingHasMoved(Side.BLACK),
                this.farRookHasMoved(Side.WHITE),
                this.nearRookHasMoved(Side.WHITE),
                this.kingHasMoved(Side.WHITE));

        // either remove black and add white or reverse. Same operation.
        hashCode ^= rngTable.getBlackToMoveRandom();

        hashCodeFreq = incrementHashCodeFrequency(hashCode);

        // show that this move is now the last move made
        moveHistory.push(new Move(move));

        // move was made, next player's turn
        turn = nextSide;

        return true;
    }

    private void movePiece(final Piece pieceMoving, final int toRow, final int toCol, final Move.MoveNote note) {

        final long bitMove = getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ getMask(toRow, toCol);

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old hash from where piece was
        hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), pieceMoving.getRow(), pieceMoving.getCol());

        // remove pieces old position
        board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
        // update board to reflect piece's new position
        board[toRow][toCol] = pieceMoving;

        // tell piece its new position
        pieceMoving.setPos(toRow, toCol);
        pieceMoving.setMoved(true);

        if (note == Move.MoveNote.NEW_QUEEN) {
            pieceMoving.setPieceID(QUEEN);
            posBitBoard[PAWN][pieceMoving.getSide()] ^= pieceMoving.getBit();
            posBitBoard[QUEEN][pieceMoving.getSide()] ^= pieceMoving.getBit();
        } else if (note == Move.MoveNote.NEW_KNIGHT) {
            pieceMoving.setPieceID(KNIGHT);
            posBitBoard[PAWN][pieceMoving.getSide()] ^= pieceMoving.getBit();
            posBitBoard[KNIGHT][pieceMoving.getSide()] ^= pieceMoving.getBit();
        }

        // add hash of piece at new location
        hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), toRow, toCol);
    }

    public long undoMove() {

        // if no there is no last move then undoMove is impossible
        if (moveHistory.isEmpty()) {
            // logger.debug("Can not undo move");
            return 0;
        }

        // retrieve last move made
        final long lastMove = getLastMoveMade();

        final int fromRow = Move.getFromRow(lastMove);
        final int fromCol = Move.getFromCol(lastMove);
        final int toRow = Move.getToRow(lastMove);
        final int toCol = Move.getToCol(lastMove);
        final Move.MoveNote note = Move.getNote(lastMove);

        // last move made was made by previous player, which is also the next
        // player
        final int prevSide = turn;
        turn = Side.otherSide(turn);

        if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_FAR) {
                rook = board[materialRow[turn]][3];

                undoMovePiece(king, materialRow[turn], kingStartCols[turn], Move.MoveNote.NONE, false);
                undoMovePiece(rook, materialRow[turn], rookStartCols[turn][0], Move.MoveNote.NONE, false);

                board[materialRow[turn]][kingStartCols[turn]] = king;
                board[materialRow[turn]][rookStartCols[turn][0]] = rook;

            } else {

                rook = board[materialRow[turn]][5];

                undoMovePiece(king, materialRow[turn], kingStartCols[turn], Move.MoveNote.NONE, false);
                undoMovePiece(rook, materialRow[turn], rookStartCols[turn][1], Move.MoveNote.NONE, false);

                board[materialRow[turn]][kingStartCols[turn]] = king;
                board[materialRow[turn]][rookStartCols[turn][1]] = rook;

            }

            castleHistory[turn] = HAS_NOT_CASTLED;

        } else {
            undoMovePiece(board[toRow][toCol], fromRow, fromCol, note, Move.hadMoved(lastMove));
        }

        if (Move.hasPieceTaken(lastMove)) {

            // add taken piece back to vectors and board
            final Piece pieceTaken = piecesTaken[prevSide].pop();

            pieces[prevSide].add(pieceTaken);

            // add piece taken to position bit board
            posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] |= pieceTaken.getBit();
            allPosBitBoard[pieceTaken.getSide()] |= pieceTaken.getBit();

            board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

        }

        //decrement old hashcode frequency
        hashCodeFreq = decrementHashCodeFrequency(hashCode);

        // move was undone so show move made before that as the last move made
        moveHistory.pop();

        if (hashCodeHistory.isEmpty()) {
            // if no hashCode was saved then generate it the hard way
            hashCode = generateHashCode();
        } else {
            // retrieve what the hashCode was before move was made
            hashCode = hashCodeHistory.pop();
        }

        return lastMove;
    }

    private void undoMovePiece(final Piece pieceMoving, final int fromRow, final int fromCol, final Move.MoveNote note, final boolean hadMoved) {

        final long bitMove = getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ getMask(fromRow, fromCol);

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old position
        board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
        // put piece in old position
        board[fromRow][fromCol] = pieceMoving;

        // tell piece where it was
        pieceMoving.setPos(fromRow, fromCol);

        // show whether piece had moved before this move was made
        pieceMoving.setMoved(hadMoved);

        if (note == Move.MoveNote.NEW_QUEEN) {
            pieceMoving.setPieceID(PAWN);
            posBitBoard[PAWN][pieceMoving.getSide()] |= pieceMoving.getBit();
            posBitBoard[QUEEN][pieceMoving.getSide()] ^= pieceMoving.getBit();
        } else if (note == Move.MoveNote.NEW_KNIGHT) {
            pieceMoving.setPieceID(PAWN);
            posBitBoard[PAWN][pieceMoving.getSide()] |= pieceMoving.getBit();
            posBitBoard[KNIGHT][pieceMoving.getSide()] ^= pieceMoving.getBit();
        }

    }

    public boolean canUndo() {
        return (moveHistory.size() != 0);
    }

    public ArrayList<Long> generateValidMoves() {
        return generateValidMoves(0, AI.noKillerMoves);
    }

    public ArrayList<Long> generateValidMoves(long hashMove, long[] killerMoves) {

        validMoves.clear();

        int prevMovesSize = 0;

        Long move;
        for (Piece p : pieces[turn]) {

            p.generateValidMoves(this, nullMoveInfo, allPosBitBoard, validMoves);

            for (int m = prevMovesSize; m < validMoves.size(); m++) {
                move = validMoves.get(m);

                move = Move.setHadMoved(move, p.hasMoved());

                if (move == hashMove) {
                    move = Move.setValue(move, 10000);
                    validMoves.set(m, move);
                    continue;
                }

                for (int k = 0; k < killerMoves.length; k++) {
                    if (move == killerMoves[k]) {
                        move = Move.setValue(move, 9999 - k);
                        break;
                    }
                }

                validMoves.set(m, move);

            }

            prevMovesSize = validMoves.size();

        }

        return validMoves;
    }

    public long[] getAllPosBitBoard() {
        return allPosBitBoard;
    }

    public long[][] getPosBitBoard() {
        return posBitBoard;
    }

    public long[] makeNullMove() {
        // long nullMoveAttacks = 0;
        // long inCheckArrayList = ALL_ONES;
        // long bitAttackCompliment = 0;
        //
        // nullMoveInfo[0] = nullMoveAttacks;
        // nullMoveInfo[1] = inCheckArrayList;
        // nullMoveInfo[2] = bitAttackCompliment;

        // recalculating check info
        clearBoardStatus();
        pieces[turn].forEach(Piece::clearBlocking);

        final int otherSide = Side.otherSide(turn);
        nullMoveInfo[0] = Pawn.getPawnAttacks(posBitBoard[PAWN][otherSide], otherSide);
        nullMoveInfo[0] |= Knight.getKnightAttacks(posBitBoard[KNIGHT][otherSide]);
        nullMoveInfo[0] |= King.getKingAttacks(posBitBoard[KING][otherSide]);

        final long jumperAttacks = Pawn.getPawnAttacks(posBitBoard[KING][turn], turn) & posBitBoard[PAWN][otherSide] |
                Knight.getKnightAttacks(posBitBoard[KING][turn]) & posBitBoard[KNIGHT][otherSide];

        nullMoveInfo[1] = jumperAttacks == 0 ? ALL_ONES : (hasOneBitOrLess(jumperAttacks) ? jumperAttacks : 0);
        nullMoveInfo[2] = 0;

        final long friendOrFoe = allPosBitBoard[0] | allPosBitBoard[1];
        final long updown = ~friendOrFoe;
        final long left = NOT_LEFT1 & updown;
        final long right = NOT_RIGHT1 & updown;
        final long kingCheckVectors = King.getKingCheckVectors(posBitBoard[KING][turn], updown, left, right);

        for (Piece p : pieces[otherSide]) {
            p.getNullMoveInfo(this,
                    nullMoveInfo,
                    updown,
                    left,
                    right,
                    posBitBoard[KING][turn],
                    kingCheckVectors,
                    allPosBitBoard[turn]);
        }

        if ((kings[turn].getBit() & nullMoveInfo[0]) != 0) {
            setBoardStatus(Game.GameStatus.CHECK);
        }

        return nullMoveInfo;
    }

    public Piece.PositionStatus checkPiece(int row, int col, int player) {

        if (((row | col) & (~0x7)) != 0) {
            return Piece.PositionStatus.OFF_BOARD;
        }

        if (board[row][col] != null) {
            if (board[row][col].getSide() == player)
                return Piece.PositionStatus.FRIEND;
            else
                return Piece.PositionStatus.ENEMY;
        } else {
            return Piece.PositionStatus.NO_PIECE;
        }

    }

    public long getLastMoveMade() {
        if (!moveHistory.isEmpty()) {
            return moveHistory.peek().getMoveLong();
        } else {
            return 0;
        }
    }

    public Deque<Move> getMoveHistory() {
        return moveHistory;
    }

    List<Piece>[] getPieces() {
        return this.pieces;
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public int getPieceID(int row, int col) {
        if (board[row][col] != null) {
            return board[row][col].getPieceID();
        } else {
            return NONE;
        }
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Deque<Piece> getPiecesTakenFor(int player) {
        return piecesTaken[player];
    }

    private void initializeCastleSetup() {

        int[][] rookCols = {{-1, -1}, {-1, -1}};
        this.rookStartCols = rookCols;

        for (int s = 0; s < 2; s++) {
            for (int c = kings[s].getCol() - 1; c >= 0; c--) {

                if (board[materialRow[s]][c] != null) {
                    if (board[materialRow[s]][c].getPieceID() == ROOK) {
                        rookCols[s][0] = c;
                        break;
                    }
                }
            }

            for (int c = kings[s].getCol() + 1; c < 8; c++) {
                if (board[materialRow[s]][c] != null) {
                    if (board[materialRow[s]][c].getPieceID() == ROOK) {
                        rookCols[s][1] = c;
                        break;
                    }
                }
            }
        }

        kingStartCols[Side.BLACK] = kings[Side.BLACK].getCol();
        kingStartCols[Side.WHITE] = kings[Side.WHITE].getCol();
    }

    public boolean placePiece(Piece piece, int toRow, int toCol) {

        if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
            if (board[toRow][toCol] != null) {
                if (board[toRow][toCol].getPieceID() == KING) {
                    return false;
                }
            }
        }

        if (piece.getPieceID() == KING) {
            if (toRow < 0 || toCol < 0) {
                return false;
            } else {
                kings[piece.getSide()] = piece;
            }
        }

        piece.setMoved(false);

        if (piece.getRow() >= 0) {
            // remove where piece was if it was on board
            posBitBoard[piece.getPieceID()][piece.getSide()] ^= piece.getBit();
            allPosBitBoard[piece.getSide()] ^= piece.getBit();
            board[piece.getRow()][piece.getCol()] = null;

        } else {

            pieces[piece.getSide()].add(piece);
        }

        if (toRow >= 0) {
            // remove where piece taken was
            if (board[toRow][toCol] != null) {

                Piece pieceTaken = board[toRow][toCol];

                // remove bit position of piece taken
                posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] ^= pieceTaken.getBit();
                allPosBitBoard[pieceTaken.getSide()] ^= pieceTaken.getBit();

                // remove ref to piece taken
                pieces[pieceTaken.getSide()].remove(pieceTaken);
            }

            // tell piece where it is now
            piece.setPos(toRow, toCol);

            // reflect new piece in position bitboard
            posBitBoard[piece.getPieceID()][piece.getSide()] |= piece.getBit();
            allPosBitBoard[piece.getSide()] |= piece.getBit();

            // update board ref to show piece there
            board[toRow][toCol] = piece;
        } else {
            // piece is being taken off the board. Remove
            if (piece.getPieceID() != KING) {
                pieces[piece.getSide()].remove(piece);
            }
        }

        // basically start over with new board
        this.moveHistory.clear();
        this.hashCodeHistory.clear();

        this.hashCode = generateHashCode();

        initializeCastleSetup();

        return true;
    }

    public boolean isInCheck() {
        return (boardStatus == Game.GameStatus.CHECK);
    }

    public boolean isInCheckMate() {
        return (boardStatus == Game.GameStatus.CHECKMATE);
    }

    public boolean isInStaleMate() {
        return (boardStatus == Game.GameStatus.STALEMATE);
    }

    public boolean isTimeUp() {
        return (boardStatus == Game.GameStatus.TIMES_UP);
    }

    public boolean isDraw() {
        return (boardStatus == Game.GameStatus.DRAW);
    }

    public boolean isInvalid() {
        return (boardStatus == Game.GameStatus.INVALID);
    }

    public boolean isGameOver() {
        return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw() || isInvalid());
    }

    public void clearBoardStatus() {
        boardStatus = Game.GameStatus.IN_PLAY;
    }

    public Game.GameStatus getBoardStatus() {
        return boardStatus;
    }

    public void setBoardStatus(Game.GameStatus boardStatus) {
        this.boardStatus = boardStatus;
    }

    private Piece farRook(int player) {
        if (rookStartCols[player][0] != -1) {
            return board[materialRow[player]][rookStartCols[player][0]];
        } else {
            return null;
        }
    }

    public boolean farRookHasMoved(int player) {
        Piece p = farRook(player);
        return p == null || p.hasMoved();
    }

    private Piece nearRook(int player) {
        if (rookStartCols[player][1] != -1) {
            return board[materialRow[player]][rookStartCols[player][1]];
        } else {
            return null;
        }
    }

    public boolean nearRookHasMoved(int player) {
        Piece p = nearRook(player);
        return p == null || p.hasMoved();
    }

    public void applyCastleRights(int player, boolean nearRights, boolean farRights) {
        Piece nearRook = nearRook(player);
        Piece farRook = farRook(player);

        if (nearRook != null) {
            nearRook.setMoved(!nearRights);
        }
        if (farRook != null) {
            farRook.setMoved(!farRights);
        }
    }

    public int getRookStartingCol(int side, int near) {
        return rookStartCols[side][near];
    }

    public boolean kingHasMoved(int player) {
        return kings[player].hasMoved();
    }

    public Board copy() {
        return new Board(pieces, turn, moveHistory, rookStartCols, kingStartCols);
    }

    public String toString() {
        String stringBoard = "";
        int pieceDetails = 0;
        Piece p;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null) {

                    p = board[row][col];

                    if (p.hasMoved() && (p.getPieceID() == PAWN || p.getPieceID() == KING || p.getPieceID() == ROOK)) {
                        pieceDetails |= 1;
                    }

                    if (p.getPieceID() == ROOK && kingHasMoved(p.getSide())) {
                        pieceDetails |= 1;
                    }

                    if (p.getPieceID() == KING && nearRookHasMoved(p.getSide()) && farRookHasMoved(p.getSide())) {
                        pieceDetails |= 1;
                    }

                    long lastMove = getLastMoveMade();

                    if (getLastMoveMade() != 0) {
                        if (Move.getToRow(lastMove) == row && Move.getToCol(lastMove) == col && Move.getNote(lastMove) == Move.MoveNote.PAWN_LEAP) {
                            pieceDetails |= 2;
                        }
                    }

                    stringBoard += board[row][col].toString() + pieceDetails + ",";
                    pieceDetails = 0;

                } else {
                    stringBoard += "__,";
                }

            }
            stringBoard += "\n";
        }

        return stringBoard;
    }

    public String toXML(boolean includeHistory) {
        return XMLParser.boardToXML(this, includeHistory);
    }

    public long generateHashCode() {
        long hashCode = 0;

        if (turn == Side.BLACK) {
            hashCode = rngTable.getBlackToMoveRandom();
        }

        Piece p;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                p = board[r][c];
                if (p != null) {
                    hashCode ^= rngTable.getPiecePerSquareRandom(p.getSide(), p.getPieceID(), r, c);
                }
            }
        }

        hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
                this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

        if (getLastMoveMade() != 0) {
            if (Move.getNote(getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {
                hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
            }
        }

        return hashCode;
    }

    public long getHashCode() {
        return hashCode;
    }

    private int incrementHashCodeFrequency(long hashCode) {
        return Optional.ofNullable(
                hashCodeFrequencies.put(hashCode, Optional.ofNullable(
                        hashCodeFrequencies.get(hashCode))
                        .orElse(0) + 1))
                .orElse(0) + 1;
    }

    private int decrementHashCodeFrequency(long hashCode) {
        return Optional.ofNullable(hashCodeFrequencies.get(hashCode))
                .map(i -> {
                    i--;
                    if (i == 0) {
                        hashCodeFrequencies.remove(hashCode);
                    } else {
                        hashCodeFrequencies.put(hashCode, i);
                    }
                    return i;
                }).orElse(0);
    }

    public int getHashCodeFreq() {
        return this.hashCodeFreq;
    }

    public boolean drawByThreeRule() {
        return hashCodeFreq >= 3;
    }

    public boolean insufficientMaterial() {

        for (List<Piece> ps : pieces) {
            for (Piece p : ps) {
                if ((p.getPieceID() == PAWN) || (p.getPieceID() == QUEEN) || (p.getPieceID() == ROOK)) {
                    return false;
                }
            }
        }

        return true;
    }

    public int[] getCastleHistory() {
        return castleHistory;
    }

    private void loadPiecesTaken() {

        for (int i = 0; i < pieces.length; i++) {

            List<Piece> tempPieces = BoardFactory.getFullPieceSet(i);

            for (int p = 0; p < pieces[i].size(); p++) {
                Piece piecePresent = pieces[i].get(p);

                for (int t = 0; t < tempPieces.size(); t++) {
                    if (tempPieces.get(t).getPieceID() == piecePresent.getPieceID()) {
                        tempPieces.remove(t);
                        break;
                    }
                }
            }

            piecesTaken[i] = new ArrayDeque<>(tempPieces);
        }

    }

}
