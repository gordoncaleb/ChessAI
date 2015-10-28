package com.gordoncaleb.chess;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.pieces.Piece;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveTest {
    private static final Logger logger = LoggerFactory.getLogger(Move.class);

    @Test
    public void test() {

        long moveLong;
        Piece pieceTaken1;
        Piece pieceTaken2;

        for (int i = 0; i < 2000000; i++) {
            moveLong = Move.moveLong(1, 2, 3, 4, i, Move.MoveNote.NONE, null, true);

            //moveLong = Move.setValue(moveLong, -i);


            if (Move.getValue(moveLong) != i) {
                logger.debug("problem at " + i + "!=" + Move.getValue(moveLong));
                logger.debug("it is 0x" + Integer.toHexString(Move.getValue(moveLong)));
            }
        }

        // for (int n = 0; n < MoveNote.values().length; n++) {
        // for (int ptid = 0; ptid < PieceID.values().length; ptid++) {
        // for (int ptc = 0; ptc < 8; ptc++) {
        // for (int ptr = 0; ptr < 8; ptr++) {
        // for (int fr = 0; fr < 8; fr++) {
        // for (int fc = 0; fc < 8; fc++) {
        // for (int tr = 0; tr < 8; tr++) {
        // for (int tc = 0; tc < 8; tc++) {
        // pieceTaken1 = new Piece(PieceID.PAWN, Side.WHITE, 1, 2, false);
        // moveLong = Move.moveLong(fr, fc, tr, tc, tr - tc, MoveNote.NONE,
        // pieceTaken1, true);
        //
        // pieceTaken2 = new Piece(PieceID.values()[ptid], Side.WHITE, ptr, ptc,
        // true);
        // moveLong = Move.setPieceTaken(moveLong, pieceTaken2);
        //
        // moveLong = Move.setNote(moveLong, MoveNote.values()[n]);
        //
        // moveLong = Move.setHadMoved(moveLong, false);
        //
        // if (Move.getFromRow(moveLong) != fr) {
        // logger.debug("getFromRow");
        // }
        // if (Move.getFromCol(moveLong) != fc) {
        // logger.debug("getFromCol");
        // }
        // if (Move.getToRow(moveLong) != tr) {
        // logger.debug("getToRow");
        // }
        // if (Move.getToCol(moveLong) != tc) {
        // logger.debug("getToCol");
        // }
        // if (Move.getNote(moveLong) != MoveNote.values()[n]) {
        // logger.debug("getNote");
        // }
        //
        // if (Move.getPieceTakenID(moveLong) != PieceID.values()[ptid]) {
        // logger.debug("getPieceTakenID");
        // }
        //
        // if (Move.getPieceTakenRow(moveLong) != ptr) {
        // logger.debug("getPieceTakenRow");
        // }
        //
        // if (Move.getPieceTakenCol(moveLong) != ptc) {
        // logger.debug("getPieceTakenCol");
        // }
        //
        // if (!Move.getPieceTakenHasMoved(moveLong)) {
        // logger.debug("getPieceTakenHasMoved");
        // }
        //
        // if (Move.hadMoved(moveLong)) {
        // logger.debug("hadMoved");
        // }
        //
        // if (Move.getValue(moveLong) != (tr - tc)) {
        // logger.debug("value");
        // }
        //
        // }
        // }
        // }
        // }
        // }
        // }
        // }
        // }

        logger.debug("Done");
    }
}
