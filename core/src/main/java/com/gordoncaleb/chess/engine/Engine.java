package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScore;
import com.gordoncaleb.chess.ui.gui.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Engine {

    private final MoveBook moveBook;
    private final StaticScore scorer;

    private Move[][] killerMoves = new Move[100][AISettings.maxKillerMoves];
    private int[] killerMoveSize = new int[100];
    private BoardHashEntry[] hashTable = new BoardHashEntry[AISettings.hashTableSize];

    private final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private final int START_BETA = -START_ALPHA;

    public Engine(MoveBook moveBook, StaticScore scorer) {
        this.moveBook = moveBook;
        this.scorer = scorer;
    }

    public Long process(final Board board, final int depth) {

        Optional<Move> moveBookRec = moveBook.getRecommendations(board.getHashCode())
                .map(moveList -> moveList.get(0));

        clearKillerMoves();

        growDecisionTreeLite(board, START_ALPHA, START_BETA, depth, depth, null, 0);

        return 1L;
    }

    private int growDecisionTreeLite(Board board, int alpha, int beta, int level, int maxDepth, Move moveMade, int bonusLevel) {
        int suggestedPathValue;
        int bestPathValue = Integer.MIN_VALUE;

        int a = alpha;
        int b = beta;
        Move bestMove = null;

        int hashIndex = (int) (board.getHashCode() & AISettings.hashIndexMask);
        BoardHashEntry hashOut;
        Move hashMove = null;
        hashOut = hashTable[hashIndex];


        if (hashOut != null) {
            if (hashOut.getHashCode() == board.getHashCode()) {

                if (hashOut.getLevel() >= level) {

                    if (hashOut.getBounds() == BoardHashEntry.ValueBounds.PV) {
                        return hashOut.getScore();
                    } else {

                        if (hashOut.getBounds() == BoardHashEntry.ValueBounds.CUT) {
                            if (hashOut.getScore() >= beta) {
                                return hashOut.getScore();
                            } else {
                                hashMove = hashOut.getBestMove();
                            }
                        } else {
                            hashMove = hashOut.getBestMove();
                        }
                    }
                } else {
                    hashMove = hashOut.getBestMove();
                }

            }
        }

        board.makeNullMove();


        if ((board.getBoardStatus() == Game.GameStatus.CHECK) && (level > -AISettings.maxInCheckFrontierLevel)) {
            bonusLevel = Math.min(bonusLevel, level - 2);
        }

        if ((moveMade.hasPieceTaken()) && (level > -AISettings.maxPieceTakenFrontierLevel)) {
            bonusLevel = Math.min(bonusLevel, level - 1);
        }

        if (level > bonusLevel) {

            if (board.insufficientMaterial() || board.drawByThreeRule()) {
                board.setBoardStatus(Game.GameStatus.DRAW);
            } else {
                List<Move> moves = new ArrayList<>(board.generateValidMoves(hashMove, AI.noKillerMoves).toList());

                if (moves.size() == 0) {
                    if (board.isInCheck()) {
                        board.setBoardStatus(Game.GameStatus.CHECKMATE);
                    } else {
                        board.setBoardStatus(Game.GameStatus.STALEMATE);
                    }
                } else {

                    Collections.sort(moves, Collections.reverseOrder());

                    Game.GameStatus tempBoardState;
                    for (Move move : moves) {

                        tempBoardState = board.getBoardStatus();

                        board.makeMove(move);

                        suggestedPathValue = -growDecisionTreeLite(board, -beta, -alpha, level - 1, maxDepth, move, bonusLevel);

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

        if (getNodeType(bestPathValue, a, b) != BoardHashEntry.ValueBounds.ALL && level >= 0 && AISettings.useKillerMove) {
            addKillerMove(level, bestMove);
        }

        if (AISettings.useHashTable && level >= 0) {
            final int moveNum = board.getMoveHistory().size();
            if (hashOut == null) {
                hashTable[hashIndex] = new BoardHashEntry(board.getHashCode(), level, bestPathValue, moveNum, getNodeType(bestPathValue, a, b), bestMove);
            } else {
                if (hashTableUpdate(hashOut, level, moveNum)) {
                    hashOut.setAll(board.getHashCode(), level, bestPathValue, moveNum, getNodeType(bestPathValue, a, b), bestMove);// ,board.toString());
                }
            }

        }

        return bestPathValue;
    }

    private BoardHashEntry.ValueBounds getNodeType(int s, int a, int b) {
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


}
