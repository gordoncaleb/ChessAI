package com.gordoncaleb.chess.ui.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.engine.legacy.AI;
import com.gordoncaleb.chess.engine.legacy.AISettings;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.ui.gui.game.Game;
import com.gordoncaleb.chess.ui.gui.game.Player;
import com.gordoncaleb.chess.ui.gui.game.PlayerContainer;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.engine.score.PositionBonus;

public class PlayerGUI implements Player, BoardGUI, MouseListener {
    private JFrame frame;

    private JMenuItem newGameMenu;
    private JMenuItem new960GameMenu;
    private JMenuItem loadGameMenu;
    private JMenuItem saveGameMenu;
    private JMenuItem undoUserMoveMenu;
    private JMenuItem switchSidesMenu;

    private JMenuItem boardFreeSetupMenu;
    private JMenuItem flipBoardMenu;

    private JMenuItem getAIRecommendationMenu;
    private JMenuItem aiSettingsMenu;

    private BoardPanel boardPanel;

    // private AI ai;

    private PlayerContainer game;

    private JFileChooser fc = new JFileChooser();

    public static void main(String[] args) {
        PositionBonus.applyScale();

        PlayerGUI playerOne = new PlayerGUI(null, false);
        AI playerTwo = new AI(null);
        playerTwo.setUseBook(true);

        Hashtable<Integer, Player> players = new Hashtable<Integer, Player>();

        int humanSide = playerOne.optionForSide();

        players.put(humanSide, playerOne);
        players.put(Side.otherSide(humanSide), playerTwo);

        Game game = new Game(players);

        playerOne.setGame(game);
        playerTwo.setGame(game);

        game.newGame(BoardFactory.getStandardChessBoard(), false);

    }

    public PlayerGUI(PlayerContainer game, boolean debug) {
        this.game = game;

        frame = new JFrame("Oh,Word? " + AISettings.version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenu AIMenu = new JMenu("AI");
        JMenu boardMenu = new JMenu("Board");
        JMenu cheatMenu = new JMenu("Cheat");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        newGameMenu = new JMenuItem("New Game");
        newGameMenu.addMouseListener(this);
        gameMenu.add(newGameMenu);

        new960GameMenu = new JMenuItem("New Chess960 Game");
        new960GameMenu.addMouseListener(this);
        gameMenu.add(new960GameMenu);

        undoUserMoveMenu = new JMenuItem("Undo Last Move");
        undoUserMoveMenu.addMouseListener(this);
        cheatMenu.add(undoUserMoveMenu);

        switchSidesMenu = new JMenuItem("Switch Side");
        switchSidesMenu.addMouseListener(this);
        cheatMenu.add(switchSidesMenu);

        loadGameMenu = new JMenuItem("Load Game");
        loadGameMenu.addMouseListener(this);
        gameMenu.add(loadGameMenu);

        saveGameMenu = new JMenuItem("Save Game");
        saveGameMenu.addMouseListener(this);
        gameMenu.add(saveGameMenu);

        boardFreeSetupMenu = new JMenuItem("Board Setup");
        boardFreeSetupMenu.addMouseListener(this);
        boardMenu.add(boardFreeSetupMenu);

        flipBoardMenu = new JMenuItem("Flip Board");
        flipBoardMenu.addMouseListener(this);
        boardMenu.add(flipBoardMenu);

        getAIRecommendationMenu = new JMenuItem("Reccomendation");
        getAIRecommendationMenu.addMouseListener(this);
        AIMenu.add(getAIRecommendationMenu);

        aiSettingsMenu = new JMenuItem("Ai Settings");
        aiSettingsMenu.addMouseListener(this);
        AIMenu.add(aiSettingsMenu);

        menuBar.add(gameMenu);
        menuBar.add(boardMenu);
        menuBar.add(AIMenu);
        menuBar.add(cheatMenu);
        frame.setJMenuBar(menuBar);

        // frame.setSize(gameWidth, gameHeight);
        //frame.setResizable(false);
        frame.setVisible(true);

        boardPanel = new BoardPanel(this, debug);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.pack();

    }

    public int optionForSide() {

        int playerSide;

        Object[] options = {"White", "Black"};
        int n = JOptionPane.showOptionDialog(frame, "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);

        if (n == JOptionPane.YES_OPTION) {
            playerSide = Side.WHITE;
            boardPanel.setFlipBoard(false);

        } else {
            playerSide = Side.BLACK;
            boardPanel.setFlipBoard(true);
        }

        return playerSide;
    }

    public synchronized void newGame(Board board) {
        boardPanel.newGame(board);

        undoUserMoveMenu.setEnabled(boardPanel.canUndo() && (boardPanel.getBoard().getMoveHistory().size() > 1));
    }

    public void gameOverLose() {
        Object[] options = {"Yes, please", "Nah"};
        int n = JOptionPane.showOptionDialog(frame, "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);

        if (n == JOptionPane.YES_OPTION) {
            promtSaveGame();
            try {
                game.newGame(null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            promtSaveGame();
        }

    }

    public void gameOverWin() {
        Object[] options = {"Yeah, why not?", "Nah."};
        int n = JOptionPane.showOptionDialog(frame, "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);

        if (n == JOptionPane.YES_OPTION) {
            promtSaveGame();
            try {
                game.newGame(null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            promtSaveGame();
        }

    }

    public void gameOverDraw() {
        Object[] options = {"Yes, please", "Nah, maybe later."};
        int n = JOptionPane.showOptionDialog(frame, "Draw...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);
        if (n == JOptionPane.YES_OPTION) {
            promtSaveGame();
            try {
                game.newGame(null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            promtSaveGame();
        }
    }

    @Override
    public void makeMove(Move move) {
        try {
            game.makeMove(move);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean moveMade(Move move) {

        boolean suc = boardPanel.moveMade(move);
        undoUserMoveMenu.setEnabled(boardPanel.canUndo() && (boardPanel.getBoard().getMoveHistory().size() > 1));

        return suc;
    }

    public void makeMove() {
        boardPanel.makeMove();
    }

    @Override
    public Move undoMove() {

        Move suc = boardPanel.undoMove();
        undoUserMoveMenu.setEnabled(boardPanel.canUndo() && (boardPanel.getBoard().getMoveHistory().size() > 1));

        return suc;
    }

    @Override
    public void recommendationMade(Move move) {
        if (move != null) {
            boardPanel.highlightMove(move);
        }
    }

    @Override
    public void setGame(PlayerContainer game) {
        this.game = game;
    }

    @Override
    public void pause() {

    }

    public Board getBoard() {
        return boardPanel.getBoard();
    }

    @Override
    public Game.GameStatus getGameStatus() {
        return null;
    }

    @Override
    public String getVersion() {
        return "Dumb Human";
    }

    @Override
    public String getPlayerName(int side) {
        if (game != null) {
            return game.getPlayerName(side);
        } else {
            return "";
        }
    }

    @Override
    public long getPlayerTime(int side) {
        if (game != null) {
            return game.getPlayerTime(side);
        } else {
            return 0;
        }
    }

    private void saveGame() {
        int returnVal = fc.showSaveDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {
                FileIO.writeFile(fc.getSelectedFile().getPath(), boardPanel.getBoard().toJson(true), false);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }

    private void promtSaveGame() {
        Object[] options = {"Yes, please", "Nope"};
        int n = JOptionPane.showOptionDialog(frame, "Would you like to save this game?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                options[0]);
        if (n == JOptionPane.YES_OPTION) {
            saveGame();
        } else {
            // System.exit(0);
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // private JButton loadGameBtn;
        // private JButton saveGameBtn;
        // private JButton newGameBtn;
        // private JButton undoBtn;
        // private JButton redoBtn;
        //
        // private JButton boardFreeSetupBtn;
        // private JButton flipBoardBtn;
        //
        // private JButton getAIRecommendationBtn;

        if (arg0.getSource() == loadGameMenu) {
            int returnVal = fc.showOpenDialog(frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                try {
                    Board board = JSONParser.fromJSON(FileIO.readResource(fc.getSelectedFile().getPath()));

                    int side = optionForSide();
                    game.setSide(side, this);
                    game.newGame(board, false);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }

        }

        if (arg0.getSource() == saveGameMenu) {
            saveGame();
        }

        if (arg0.getSource() == newGameMenu) {
            Board board = BoardFactory.getStandardChessBoard();

            int side = optionForSide();
            game.setSide(side, this);
            try {
                game.newGame(board, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (arg0.getSource() == new960GameMenu) {
            Board board = BoardFactory.getRandomChess960Board();

            int side = optionForSide();
            game.setSide(side, this);
            try {
                game.newGame(board, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (arg0.getSource() == undoUserMoveMenu) {
            if (boardPanel.canUndo() && (boardPanel.getBoard().getMoveHistory().size() > 1)) {
                game.undoMove();
                game.undoMove();
            }

        }

        if (arg0.getSource() == switchSidesMenu) {
            boardPanel.flipBoard();
            game.switchSides();

        }

        if (arg0.getSource() == boardFreeSetupMenu) {

            if (boardPanel.isFreelyMove()) {
                try {
                    game.newGame(boardPanel.getBoard().copy(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            boardPanel.setFreelyMove(!boardPanel.isFreelyMove());

            if (boardPanel.isFreelyMove()) {
                boardFreeSetupMenu.setText("Set it up!");
            } else {
                boardFreeSetupMenu.setText("Board Setup");
            }

        }

        if (arg0.getSource() == flipBoardMenu) {
            boardPanel.flipBoard();
        }

        if (arg0.getSource() == getAIRecommendationMenu) {
            game.requestRecommendation();
        }

        if (arg0.getSource() == aiSettingsMenu) {
            new AISettingsGUI("AI Settings");
        }

    }

    public void gameOver(int winlose) {
        if (winlose > 0) {
            gameOverWin();
        } else {
            if (winlose < 0) {
                gameOverLose();
            } else {
                gameOverDraw();
            }
        }

    }

    @Override
    public void endGame() {

    }

    @Override
    public void showProgress(int progress) {
        boardPanel.showProgress(progress);
    }

    @Override
    public void requestRecommendation() {
    }

}
