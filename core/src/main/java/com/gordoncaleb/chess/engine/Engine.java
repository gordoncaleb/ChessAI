package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScore;

import java.util.Optional;
import java.util.Random;

import static com.gordoncaleb.chess.board.BoardCondition.*;

public class Engine {

    public static final int MAX_DEPTH = 20;
    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

    private final Random rand = new Random();
    private final MoveBook moveBook;
    private final StaticScore scorer;

    private final MoveContainer[] moveContainers;

    public Engine(MoveBook moveBook, StaticScore scorer) {
        this.moveBook = moveBook;
        this.scorer = scorer;
        this.moveContainers = MoveContainerFactory.buildMoveContainers(MAX_DEPTH);
    }

    public Move search(final Board board, final int depth) {
        return search(board, depth, START_ALPHA, START_BETA);
    }

    public Move search(final Board board, final int depth, final int startAlpha, final int startBeta) {

        Optional<Move> moveBookRec = moveBook.getRecommendations(board.getHashCode())
                .map(moveList -> moveList.get(rand.nextInt(moveList.size())));

        return moveBookRec.orElseGet(() -> {
            searchTree(board, startAlpha, startBeta, depth, depth);
            return moveContainers[0].get(0);
        });
    }

    public int searchTree(final Board board, int alpha, final int beta, final int level, final int maxDepth) {
        int bestPathValue = Integer.MIN_VALUE;

        int a = alpha;
        int b = beta;
        Move bestMove = null;

        int condition = getBoardCondition(board);

        if (level > maxDepth) {

            if (condition != DRAW) {

                board.makeNullMove();
                MoveContainer moves = board.generateValidMoves(moveContainers[maxDepth - level]);

                if (moves.isEmpty()) {
                    if (condition == CHECK) {
                        //checkmate
                        bestPathValue = -(Values.CHECKMATE_MOVE - (maxDepth - level));
                    } else {
                        //stalemate
                        bestPathValue = 0;
                    }
                } else {

                    //try and order moves to maximize pruning
                    moves.sort();

                    for (int m = 0; m < moves.size(); m++) {

                        final Move move = moves.get(m);

                        board.makeMove(move);

                        final int suggestedPathValue = -searchTree(board, -beta, -alpha, level - 1, maxDepth);

                        board.undoMove();

                        if (suggestedPathValue > bestPathValue) {
                            bestPathValue = suggestedPathValue;
                            bestMove = move;
                        }

                        if (bestPathValue > alpha) {
                            //narrowing ab window
                            alpha = bestPathValue;
                        }

                        if (alpha >= beta) {
                            //pruned!
                            break;
                        }

                    }

                }
            } else {
                bestPathValue = 0;
            }
        } else {
            bestPathValue = scorer.staticScore(board);
        }

        return bestPathValue;
    }

    private int getBoardCondition(Board board) {
        if (board.isInCheck()) {
            return CHECK;
        } else {
            return board.isDraw() ? DRAW : IN_PLAY;
        }
    }

}
