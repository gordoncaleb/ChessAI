package com.gordoncaleb.chess.engine.score;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.KNIGHT;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.PAWN;
import static com.gordoncaleb.chess.engine.score.Values.*;
import static com.gordoncaleb.chess.engine.score.Values.PAWN_VALUE;

public class MaterialScorer implements BoardScorer {

    @Override
    public int staticScore(Board b) {
        final int side = b.getTurn();
        return materialScoreDelta(side, Side.otherSide(side), b.getPosBitBoard());
    }

    @Override
    public int endOfGameValue(boolean isInCheck, int level) {
        return 0;
    }

    @Override
    public int drawValue() {
        return 0;
    }

    public int materialScoreDelta(final int friendSide, final int foeSide, final long[][] bitBoards) {
        return (Long.bitCount(bitBoards[QUEEN][friendSide]) - Long.bitCount(bitBoards[QUEEN][foeSide])) * QUEEN_VALUE +
                (Long.bitCount(bitBoards[ROOK][friendSide]) - Long.bitCount(bitBoards[ROOK][foeSide])) * ROOK_VALUE +
                (Long.bitCount(bitBoards[BISHOP][friendSide]) - Long.bitCount(bitBoards[BISHOP][foeSide])) * BISHOP_VALUE +
                (Long.bitCount(bitBoards[KNIGHT][friendSide]) - Long.bitCount(bitBoards[KNIGHT][foeSide])) * KNIGHT_VALUE +
                (Long.bitCount(bitBoards[PAWN][friendSide]) - Long.bitCount(bitBoards[PAWN][foeSide])) * PAWN_VALUE;
    }
}
