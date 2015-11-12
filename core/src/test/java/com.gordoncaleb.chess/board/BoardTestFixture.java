package com.gordoncaleb.chess.board;


import java.util.List;

public class BoardTestFixture {

    public static void makeRandomMoves(Board board, int numberOfMoves){
        for (int i = 0; i < numberOfMoves; i++) {
            board.makeNullMove();
            List<Move> moves = board.generateValidMoves().toList();
            moves.stream()
                    .findFirst()
                    .ifPresent(move -> board.makeMove(move));
        }
    }
}
