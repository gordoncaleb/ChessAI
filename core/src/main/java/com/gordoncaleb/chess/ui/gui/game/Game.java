package com.gordoncaleb.chess.ui.gui.game;

import java.util.Hashtable;
import java.util.ArrayList;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.DecisionNode;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.engine.score.StaticScore;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.serdes.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game implements PlayerContainer {
    private static final Logger logger = LoggerFactory.getLogger(PlayerContainer.class);

    private Hashtable<Integer, Player> players;
    private ArrayList<Player> observers;
    private int turn;
    private boolean paused;
    private GameClock clock;
    private Adjudicator adjudicator;
    private StaticScore scorer = new StaticScore();

    private Boolean gameActive;

    public Game(Hashtable<Integer, Player> players) {

        paused = false;

        this.observers = new ArrayList<Player>();

        this.players = players;

        gameActive = new Boolean(true);

    }

    public GameResults newGame(boolean block) {

        String xmlBoard = FileIO.readResource("tempSave.xml");

        if (xmlBoard == null) {
            return newGame(BoardFactory.getStandardChessBoard(), block);
        } else {
            return newGame(XMLParser.XMLToBoard(xmlBoard), block);
        }

    }

    public GameResults newGame(Board board, boolean block) {

        if (board == null) {
            board = XMLParser.XMLToBoard(FileIO.readResource("default.xml"));
        }

        turn = board.getTurn();

        adjudicator = new Adjudicator(board.copy());
        adjudicator.getValidMoves();

        clock = new GameClock("White", "Black", 0, 0, turn);

        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).newGame(board.copy());
        }

        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).newGame(board.copy());
            players.get(Side.BLACK).newGame(board.copy());
            players.get(turn).makeMove();
        } else {
            players.get(Side.BOTH).newGame(board.copy());
            players.get(Side.BOTH).makeMove();
        }

        showProgress(0);

        if (block) {

            synchronized (gameActive) {
                try {
                    gameActive.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return new GameResults(adjudicator.getGameStatus(),
                    adjudicator.getWinner(),
                    -scorer.staticScore(adjudicator.getBoard()),
                    clock.getTime(Side.WHITE),
                    clock.getTime(Side.BLACK),
                    adjudicator.getMoveHistory().size(),
                    clock.getMaxTime(Side.WHITE),
                    clock.getMaxTime(Side.BLACK));
        }

        return null;

    }

    public void showProgress(int progress) {
        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).showProgress(progress);
            players.get(Side.BLACK).showProgress(progress);
        } else {
            players.get(Side.BOTH).showProgress(progress);
        }

    }

    public void requestRecommendation() {
        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).requestRecommendation();
            players.get(Side.BLACK).requestRecommendation();
        } else {
            players.get(Side.BOTH).requestRecommendation();
        }

        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).requestRecommendation();
        }
    }

    public void recommendationMade(Move move) {
        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).recommendationMade(move);
            players.get(Side.BLACK).recommendationMade(move);
        } else {
            players.get(Side.BOTH).recommendationMade(move);
        }
    }

    public synchronized void switchSides() {
        Player whitePlayer = players.get(Side.WHITE);
        players.put(Side.WHITE, players.get(Side.BLACK));
        players.put(Side.BLACK, whitePlayer);

        if (players.get(Side.BOTH) == null) {
            players.get(turn).makeMove();
        } else {
            players.get(Side.BOTH).makeMove();
        }
    }

    public synchronized void setSide(int side, Player player) {
        if (players.get(side) != player) {
            Player whitePlayer = players.get(Side.WHITE);
            players.put(Side.WHITE, players.get(Side.BLACK));
            players.put(Side.BLACK, whitePlayer);
        }
    }

    public synchronized boolean undoMove() {

        if (adjudicator.canUndo()) {
            adjudicator.undo();
            adjudicator.getValidMoves();

            if (players.get(Side.BOTH) == null) {
                players.get(Side.WHITE).undoMove();
                players.get(Side.BLACK).undoMove();
            } else {
                players.get(Side.BOTH).undoMove();
            }

            for (int i = 0; i < observers.size(); i++) {
                observers.get(i).undoMove();
            }

            turn = Side.otherSide(turn);

            return true;

        } else {
            return false;
        }
    }

    public synchronized boolean makeMove(Move move) {

        if (clock.hit()) {
            logger.debug("Game Over " + Side.otherSide(turn) + " wins by time!");
            // adjudicator.getBoard().setBoardStatus(GameStatus.TIMES_UP);

            synchronized (gameActive) {
                gameActive.notifyAll();
            }

            return false;
        }

        turn = Side.otherSide(turn);

        if (adjudicator.move(move)) {

            adjudicator.getValidMoves();

            logger.debug("GamePhase = " + scorer.calcGamePhase(adjudicator.getBoard()));

            for (int i = 0; i < observers.size(); i++) {
                observers.get(i).moveMade(move);
            }

            if (players.get(Side.BOTH) == null) {
                players.get(Side.WHITE).moveMade(move);
                players.get(Side.BLACK).moveMade(move);
            } else {
                players.get(Side.BOTH).moveMade(move);
            }

        } else {
            adjudicator.getBoard().setBoardStatus(GameStatus.INVALID);
        }

        if (adjudicator.isGameOver()) {
            logger.debug("Game over");

            if (players.get(Side.BOTH) == null) {
                if (adjudicator.getGameStatus() == GameStatus.CHECKMATE) {
                    players.get(adjudicator.getWinner()).gameOver(1);
                    players.get(Side.otherSide(adjudicator.getWinner())).gameOver(-1);
                } else {
                    players.get(Side.WHITE).gameOver(0);
                    players.get(Side.BLACK).gameOver(0);
                }
            }

            synchronized (gameActive) {
                gameActive.notifyAll();
            }

            return false;
        } else {
            if (players.get(Side.BOTH) == null) {
                players.get(turn).makeMove();
            } else {
                players.get(Side.BOTH).makeMove();
            }
        }

        return true;
    }

    public void pause() {

        paused = !paused;

        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).pause();
            players.get(Side.BLACK).pause();
        } else {
            players.get(Side.BOTH).pause();
        }

        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).pause();
        }

    }

    public String getPlayerName(int side) {
        if (players.get(side) != null) {
            return players.get(side).getVersion();
        } else {
            return "";
        }
    }

    public long getPlayerTime(int side) {
        if (clock != null) {
            return clock.getTime(side);
        } else {
            return 0;
        }
    }

    public synchronized void addObserver(Player observer) {
        // observer.newGame(Side.NONE, board.copy());
        observer.setGame(this);
        observers.add(observer);
    }

    // public int getMoveChosenPathValue(Move m) {
    // return ai.getMoveChosenPathValue(m);
    // }

    public void setDecisionTreeRoot(DecisionNode rootDecision) {
        // decisionTreeGUI.setRootDecisionTree(rootDecision);
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void endGame() {
        if (players.get(Side.BOTH) == null) {
            players.get(Side.WHITE).endGame();
            players.get(Side.BLACK).endGame();
        } else {
            players.get(Side.BOTH).endGame();
        }

        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).endGame();
        }

    }

    public static enum GameStatus {
        IN_PLAY, CHECK, CHECKMATE, STALEMATE, TIMES_UP, DRAW, INVALID
    }
}
