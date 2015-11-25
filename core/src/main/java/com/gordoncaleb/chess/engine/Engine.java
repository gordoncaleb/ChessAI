package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.legacy.AISettings;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScore;
import com.gordoncaleb.chess.ui.gui.game.Game;

import java.util.Optional;

import static com.gordoncaleb.chess.engine.BoardHashEntry.ValueBounds.*;

public class Engine {

    public static final int MAX_DEPTH = 20;
    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;
    private static final int MAX_KILLER_MOVES = 100;

    private final MoveBook moveBook;
    private final StaticScore scorer;

    private final Move[][] killerMoves = new Move[MAX_DEPTH][MAX_KILLER_MOVES];
    private final int[] killerMoveSize = new int[MAX_DEPTH];

    private final MoveContainer[] moveContainers;

    private BoardHashEntry[] hashTable;

    public Engine(MoveBook moveBook, StaticScore scorer) {
        this.moveBook = moveBook;
        this.scorer = scorer;
        this.hashTable = initHashTable();
        this.moveContainers = MoveContainerFactory.buildMoveContainers(MAX_DEPTH);
    }

    public Move search(final Board board, final int depth) {

        Optional<Move> moveBookRec = moveBook.getRecommendations(board.getHashCode())
                .flatMap(moveList -> moveList.stream().findFirst());

        return moveBookRec.orElseGet(() -> {
            clearKillerMoves();
            searchTree(board, START_ALPHA, START_BETA, depth, depth, null, 0);
            return moveContainers[0].get(0);
        });
    }

    private boolean hashOutIsPrune(BoardHashEntry hashOut, int level, int beta) {

        final boolean sufficientLevel = hashOut.getLevel() >= level;
        final boolean isPV = hashOut.getBounds() == PV;
        final boolean isCutAndBeatsBeta = hashOut.getBounds() == CUT && hashOut.getScore() >= beta;

        return (sufficientLevel && (isPV || isCutAndBeatsBeta));
    }

    private int searchTree(Board board, int alpha, int beta, int level, int maxDepth, Move moveMade, int bonusLevel) {
        int suggestedPathValue;
        int bestPathValue = Integer.MIN_VALUE;

        int a = alpha;
        int b = beta;
        Move bestMove = null;

        board.makeNullMove();

        if (level > bonusLevel) {

            if (board.insufficientMaterial() || board.drawByThreeRule()) {
                board.setBoardStatus(Game.GameStatus.DRAW);
            } else {
                MoveContainer moves = board.generateValidMoves(moveContainers[maxDepth - level]);

                if (moves.size() == 0) {
                    if (board.isInCheck()) {
                        board.setBoardStatus(Game.GameStatus.CHECKMATE);
                    } else {
                        board.setBoardStatus(Game.GameStatus.STALEMATE);
                    }
                } else {

                    moves.sort();

                    Game.GameStatus tempBoardState;
                    for (int m = 0; m < moves.size(); m++) {

                        final Move move = moves.get(m);

                        tempBoardState = board.getBoardStatus();

                        board.makeMove(move);

                        suggestedPathValue = -searchTree(board, -beta, -alpha, level - 1, maxDepth, move, bonusLevel);

                        board.undoMove();

                        board.setBoardStatus(tempBoardState);

                        if (suggestedPathValue > bestPathValue) {
                            bestPathValue = suggestedPathValue;
                            bestMove = move;
                        }

                        if (bestPathValue > alpha) {
                            alpha = bestPathValue;
                        }

                        if (alpha >= beta) {
                            break;
                        }

                    }

                }
            }
        } else {
            bestPathValue = scorer.staticScore(board);
        }

        if (board.isInCheckMate()) {
            bestPathValue = -(Values.CHECKMATE_MOVE - (maxDepth - level));
        } else {
            if (board.isInStaleMate() || board.isDraw()) {
                bestPathValue = 0;
            }
        }


        return bestPathValue;
    }

    private int getNodeType(int s, int a, int b) {
        if (s >= b) {
            return BoardHashEntry.ValueBounds.CUT;
        }

        if (s < a) {
            return BoardHashEntry.ValueBounds.ALL;
        }

        return BoardHashEntry.ValueBounds.PV;
    }

    private boolean hashTableUpdate(BoardHashEntry present, int cLevel, int cMoveNum) {
        return (present.getMoveNum() <= cMoveNum) || (cLevel > present.getLevel());
    }

    public void clearKillerMoves() {
        for (int i = 0; i < killerMoves.length; i++) {
            killerMoveSize[i] = 0;
            for (int m = 0; m < AISettings.maxKillerMoves; m++) {
                killerMoves[i][m] = null;
            }
        }
    }

    public void addKillerMove(int level, Move move) {

        if (level >= 0) {
            if (killerMoveSize[level] < AISettings.maxKillerMoves) {

                for (int i = 0; i < killerMoveSize[level]; i++) {
                    if (killerMoves[level][i] == move) {
                        return;
                    }
                }

                killerMoves[level][killerMoveSize[level]] = move;
                killerMoveSize[level]++;
            }
        }
    }

    private BoardHashEntry[] initHashTable() {
        BoardHashEntry[] hashTable = new BoardHashEntry[AISettings.hashTableSize];
        for (int i = 0; i < hashTable.length; i++) {
            hashTable[i] = new BoardHashEntry();
        }
        return hashTable;
    }

}
