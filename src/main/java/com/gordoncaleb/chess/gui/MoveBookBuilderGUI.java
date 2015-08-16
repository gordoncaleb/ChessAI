package com.gordoncaleb.chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.backend.Game;
import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.backend.Player;
import com.gordoncaleb.chess.backend.PlayerContainer;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.io.FileIO;
import com.gordoncaleb.chess.io.MoveBook;
import com.gordoncaleb.chess.io.XMLParser;
import com.gordoncaleb.chess.pieces.PositionBonus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveBookBuilderGUI implements Player, BoardGUI, MouseListener {
    private static final Logger logger = LoggerFactory.getLogger(MoveBookBuilderGUI.class);

    private JFrame frame;
    private PlayerContainer game;
    private AI ai;
    private boolean paused;
    private boolean makeRecMove;

    private BoardPanel boardPanel;
    private JList<Move> moveList;
    private DefaultListModel<Move> listModel;
    private JButton recordMbBtn;
    private JButton deleteMbEntryBtn;
    private JButton undoBtn;
    private JButton redoBtn;
    private JButton saveMbBtn;
    private JButton mbRecommendBtn;
    private JButton aiRecommendBtn;
    private JButton aiMoveBtn;
    private JButton showAISettingsBtn;
    private JButton showDecisionTreeBtn;
    private JButton showAIPossibilitiesBtn;
    private JButton freelyMoveBtn;
    private JButton clearHashTableBtn;
    private JButton loadGameBtn;
    private JButton saveGameBtn;
    private JButton newGameBtn;
    private JButton new960GameBtn;
    private JButton flipBoardBtn;

    private JFileChooser fc = new JFileChooser();

    private MoveBook moveBook;
    private boolean record;

    public static void main(String[] args) {

        PositionBonus.applyScale();

        java.util.Hashtable<Side, Player> players = new java.util.Hashtable<>();

        AI ai = new AI(null);
        MoveBookBuilderGUI mbBuilder = new MoveBookBuilderGUI(ai);

        players.put(Side.BOTH, mbBuilder);

        Game game = new Game(players);
        game.addObserver(ai);

        mbBuilder.setGame(game);
        ai.setGame(game);

        game.newGame(false);
    }

    public MoveBookBuilderGUI(AI ai) {

        this.ai = ai;
        fc.setCurrentDirectory(new File("."));

        this.moveBook = new MoveBook();
        // moveBook.loadVerboseMoveBook();
        moveBook.loadMoveBook();
        this.record = false;

        frame = new JFrame("Move Book Builder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel eastPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<Move>();
        moveList = new JList<Move>(listModel);
        moveList.setPreferredSize(new Dimension(200, 100));
        moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveList.setLayoutOrientation(JList.VERTICAL);
        moveList.setVisibleRowCount(-1);
        moveList.addMouseListener(this);

        JScrollPane scroll = new JScrollPane(moveList);

        eastPanel.add(scroll, BorderLayout.CENTER);

        JPanel controlBtnsPanel = new JPanel();

        controlBtnsPanel.setPreferredSize(new Dimension(300, 500));

        recordMbBtn = new JButton("Record");
        recordMbBtn.addMouseListener(this);

        deleteMbEntryBtn = new JButton("Delete");
        deleteMbEntryBtn.addMouseListener(this);

        undoBtn = new JButton("Undo");
        undoBtn.addMouseListener(this);

        redoBtn = new JButton("Redo");
        redoBtn.addMouseListener(this);

        saveMbBtn = new JButton("Save MB");
        saveMbBtn.addMouseListener(this);

        mbRecommendBtn = new JButton("MB Recommend");
        mbRecommendBtn.addMouseListener(this);

        aiRecommendBtn = new JButton("AI Recommend");
        aiRecommendBtn.addMouseListener(this);

        aiMoveBtn = new JButton("AI Move");
        aiMoveBtn.addMouseListener(this);

        showAISettingsBtn = new JButton("AI Settings");
        showAISettingsBtn.addMouseListener(this);

        showDecisionTreeBtn = new JButton("Show Decision Tree");
        showDecisionTreeBtn.addMouseListener(this);

        showAIPossibilitiesBtn = new JButton("Show Possibilities");
        showAIPossibilitiesBtn.addMouseListener(this);

        freelyMoveBtn = new JButton("Free Move?");
        freelyMoveBtn.addMouseListener(this);

        loadGameBtn = new JButton("Load Game");
        loadGameBtn.addMouseListener(this);

        saveGameBtn = new JButton("Save Game");
        saveGameBtn.addMouseListener(this);

        newGameBtn = new JButton("New Game");
        newGameBtn.addMouseListener(this);

        new960GameBtn = new JButton("New 960 Game");
        new960GameBtn.addMouseListener(this);

        clearHashTableBtn = new JButton("Clear Hashtable");
        clearHashTableBtn.addMouseListener(this);

        flipBoardBtn = new JButton("Flip Board");
        flipBoardBtn.addMouseListener(this);

        JPanel gameCtrlPanel = new JPanel();
        gameCtrlPanel.setPreferredSize(new Dimension(300, 150));

        JPanel boardCtrlPanel = new JPanel();
        boardCtrlPanel.setPreferredSize(new Dimension(300, 75));

        JPanel aiCtrlPanel = new JPanel();
        aiCtrlPanel.setPreferredSize(new Dimension(300, 150));

        JPanel mbCtrlPanel = new JPanel();
        mbCtrlPanel.setPreferredSize(new Dimension(300, 100));

        gameCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Game"));
        boardCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Board"));
        aiCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "AI"));
        mbCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "MoveBook"));

        mbCtrlPanel.add(recordMbBtn);
        mbCtrlPanel.add(deleteMbEntryBtn);

        mbCtrlPanel.add(saveMbBtn);
        mbCtrlPanel.add(mbRecommendBtn);
        aiCtrlPanel.add(aiRecommendBtn);
        aiCtrlPanel.add(aiMoveBtn);
        aiCtrlPanel.add(showAISettingsBtn);
        aiCtrlPanel.add(showDecisionTreeBtn);
        aiCtrlPanel.add(showAIPossibilitiesBtn);
        boardCtrlPanel.add(freelyMoveBtn);
        boardCtrlPanel.add(flipBoardBtn);
        gameCtrlPanel.add(loadGameBtn);
        gameCtrlPanel.add(newGameBtn);
        gameCtrlPanel.add(new960GameBtn);
        gameCtrlPanel.add(saveGameBtn);
        gameCtrlPanel.add(undoBtn);
        gameCtrlPanel.add(redoBtn);

        controlBtnsPanel.add(gameCtrlPanel);
        controlBtnsPanel.add(boardCtrlPanel);
        controlBtnsPanel.add(aiCtrlPanel);
        controlBtnsPanel.add(mbCtrlPanel);

        eastPanel.add(controlBtnsPanel, BorderLayout.SOUTH);

        // frame.setSize(gameWidth, gameHeight);
        // frame.setResizable(false);
        frame.setVisible(true);

        boardPanel = new BoardPanel(this, false);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(eastPanel, BorderLayout.EAST);
        // frame.add(controlPanel, BorderLayout.NORTH);
        frame.pack();

    }

    private void populateMoveList() {
        listModel.removeAllElements();

        Optional<List<Long>> recommendations = moveBook.getAllRecommendations(boardPanel.getBoard().getHashCode());

        recommendations.ifPresent(moves -> moves.stream()
                        .forEach(m -> listModel.addElement(new Move(m)))
        );

    }

    @Override
    public long undoMove() {
        long undone = boardPanel.undoMove();
        populateMoveList();

        undoBtn.setEnabled(boardPanel.canUndo());
        redoBtn.setEnabled(boardPanel.canRedo());

        return undone;
    }

    @Override
    public void newGame(Board board) {
        boardPanel.newGame(board.getCopy());

        populateMoveList();

        tempGameSave();

        undoBtn.setEnabled(boardPanel.canUndo());
        redoBtn.setEnabled(boardPanel.canRedo());

    }

    @Override
    public void setGame(PlayerContainer game) {
        this.game = game;
    }

    @Override
    public void recommendationMade(long move) {
        if (move != 0) {
            boardPanel.highlightMove(move);
        }

        if (makeRecMove) {
            game.makeMove(move);
            makeRecMove = false;
        }
    }

    @Override
    public boolean moveMade(long move) {
        boardPanel.moveMade(move);

        logger.debug("hash = " + boardPanel.getBoard().getHashCode());

        populateMoveList();

        tempGameSave();

        boardPanel.makeMove();

        undoBtn.setEnabled(boardPanel.canUndo());
        redoBtn.setEnabled(boardPanel.canRedo());

        return true;
    }

    @Override
    public void makeMove() {

        boardPanel.makeMove();
    }

    @Override
    public void makeMove(long move) {

        if (record) {
            moveBook.addEntry(boardPanel.getBoard().toXML(false), boardPanel.getBoard().getHashCode(), move);
        }

        game.makeMove(move);

    }

    public void tempGameSave() {
        FileIO.writeFile("tempSave.xml", boardPanel.getBoard().toXML(true), false);
    }

    @Override
    public void pause() {
        this.paused = !paused;

    }

    @Override
    public Game.GameStatus getGameStatus() {
        return boardPanel.getBoard().getBoardStatus();
    }

    @Override
    public Board getBoard() {
        return boardPanel.getBoard();
    }

    @Override
    public void gameOverLose() {
        // TODO Auto-generated method stub

    }

    @Override
    public void gameOverWin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void gameOverDraw() {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getSource() == recordMbBtn) {
            record = !record;
            if (record) {
                recordMbBtn.setText("Stop");
            } else {
                recordMbBtn.setText("Record");
            }
        }

        if (e.getSource() == undoBtn) {
            if (boardPanel.canUndo()) {
                game.undoMove();
            }
        }

        if (e.getSource() == redoBtn) {
            if (boardPanel.canRedo()) {
                // boardPanel.redoMove();
                game.makeMove(boardPanel.getLastUndoneMove());
            }
        }

        if (e.getSource() == deleteMbEntryBtn) {
            Move move = moveList.getSelectedValue();
            moveBook.removeEntry(boardPanel.getBoard().toXML(false), boardPanel.getBoard().getHashCode(), move);
            populateMoveList();
        }

        if (e.getSource() == saveMbBtn) {
            moveBook.saveMoveBook();
        }

        if (e.getSource() == mbRecommendBtn) {
            long rec = moveBook.getRecommendation(boardPanel.getBoard().getHashCode());

            if (rec != 0) {
                boardPanel.highlightMove(rec);
            }
        }

        if (e.getSource() == aiRecommendBtn) {

            makeRecMove = false;
            game.requestRecommendation();

        }

        if (e.getSource() == aiMoveBtn) {

            makeRecMove = true;
            game.requestRecommendation();

        }

        if (e.getSource() == showAISettingsBtn) {
            new AISettingsGUI("MoveBookBuilder AI Settings", ai);
        }

        if (e.getSource() == showDecisionTreeBtn) {
            new DecisionTreeGUI(ai);
        }

        if (e.getSource() == moveList) {

            if (moveList.getSelectedIndex() > -1) {
                long move = moveList.getSelectedValue().getMoveLong();
                boardPanel.highlightMove(move);
            }
        }

        if (e.getSource() == freelyMoveBtn) {

            boardPanel.setFreelyMove(!boardPanel.isFreelyMove());

            if (boardPanel.isFreelyMove()) {
                freelyMoveBtn.setText("Moving Freely");
            } else {
                game.newGame(boardPanel.getBoard(), false);
                freelyMoveBtn.setText("Free Move?");
                populateMoveList();
            }
        }

        if (e.getSource() == loadGameBtn) {

            int returnVal = fc.showOpenDialog(frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                Board board = XMLParser.XMLToBoard(FileIO.readResource(fc.getSelectedFile().getPath()));

                game.newGame(board, false);

            }
        }

        if (e.getSource() == saveGameBtn) {

            int returnVal = fc.showSaveDialog(frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                FileIO.writeFile(fc.getSelectedFile().getPath(), boardPanel.getBoard().toXML(true), false);

            }
        }

        if (e.getSource() == newGameBtn) {

            Board board = BoardFactory.getStandardChessBoard();

            game.newGame(board, false);

        }

        if (e.getSource() == new960GameBtn) {

            Board board = BoardFactory.getRandomChess960Board();

            game.newGame(board, false);

        }

        if (e.getSource() == flipBoardBtn) {
            boardPanel.flipBoard();
        }

        if (e.getSource() == showAIPossibilitiesBtn) {
            new PossibleBoardDisplay(ai, 4);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getVersion() {
        return "MoveBookBuilder";
    }

    @Override
    public String getPlayerName(Side side) {
        return "N/A";
    }

    @Override
    public long getPlayerTime(Side side) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void endGame() {
        // TODO Auto-generated method stub

    }

    @Override
    public void gameOver(int winlose) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showProgress(int progress) {
        boardPanel.showProgress(progress);
    }

    @Override
    public void requestRecommendation() {
    }

}
