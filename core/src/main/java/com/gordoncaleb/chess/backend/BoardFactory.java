package com.gordoncaleb.chess.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import com.gordoncaleb.chess.pieces.Piece;
import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;

public class BoardFactory {

    private static final Random r = new Random();

    public static Board getStandardChessBoard() {
        return makeBoard(1, 2, 2, 1, 2);
    }

    public static Board getRandomChess960Board() {
        return makeBoard(rollDie(4), rollDie(4), rollDie(6), rollDie(5), rollDie(4));
    }

    public static Board makeBoard(int r1, int r2, int r3, int r4, int r5) {
        ArrayList<Piece>[] pieces = new ArrayList[2];
        pieces[0] = new ArrayList<>();
        pieces[1] = new ArrayList<>();

        int[] pawnRow = new int[2];
        pawnRow[Side.BLACK.ordinal()] = 1;
        pawnRow[Side.WHITE.ordinal()] = 6;

        int[] mainRow = new int[2];
        mainRow[Side.BLACK.ordinal()] = 0;
        mainRow[Side.WHITE.ordinal()] = 7;

        // public Piece(PieceID id, Side player, int row, int col, boolean
        // moved) {
        Piece temp;
        for (int s = 0; s < 2; s++) {
            for (int p = 0; p < 8; p++) {
                temp = new Piece(Piece.PieceID.PAWN, Side.values()[s], pawnRow[s], p, false);
                pieces[s].add(temp);
            }
        }

        int[] setup = new int[8];
        Arrays.fill(setup, NONE);

        setup[r1 * 2] = BISHOP;
        setup[r2 * 2 + 1] = BISHOP;

        setup[ithEmptyPosition(r3 + 1, setup)] = QUEEN;

        setup[ithEmptyPosition(r4 + 1, setup)] = KNIGHT;
        setup[ithEmptyPosition(r5 + 1, setup)] = KNIGHT;

        setup[ithEmptyPosition(2, setup)] = KING;

        setup[ithEmptyPosition(1, setup)] = ROOK;
        setup[ithEmptyPosition(1, setup)] = ROOK;

        for (int s = 0; s < 2; s++) {
            for (int p = 0; p < 8; p++) {
                temp = new Piece(setup[p], Side.values()[s], mainRow[s], p, false);
                pieces[s].add(temp);
            }
        }

        return new Board(pieces, Side.WHITE, new Stack<>(), null, null);
    }

    private static int ithEmptyPosition(int i, int[] setup) {
        for (int n = 0; n < setup.length; n++) {

            if (setup[n] == NONE) {
                i--;
            }

            if (i <= 0) {
                return n;
            }
        }

        return setup.length;
    }

    public static int rollDie(int dieSize) {
        return r.nextInt(dieSize);
    }

    public static Stack<Piece> getFullPieceSet(Side player) {
        Stack<Piece> pieces = new Stack<>();

        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(Piece.PieceID.PAWN, player, 0, 0, false));
        }

        for (int i = 0; i < 2; i++) {
            pieces.add(new Piece(Piece.PieceID.BISHOP, player, 0, 0, false));
        }

        for (int i = 0; i < 2; i++) {
            pieces.add(new Piece(Piece.PieceID.ROOK, player, 0, 0, false));
        }

        for (int i = 0; i < 2; i++) {
            pieces.add(new Piece(Piece.PieceID.KNIGHT, player, 0, 0, false));
        }

        pieces.add(new Piece(Piece.PieceID.KING, player, 0, 0, false));
        pieces.add(new Piece(Piece.PieceID.QUEEN, player, 0, 0, false));

        return pieces;
    }

}
