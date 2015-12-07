package com.gordoncaleb.chess.ui.gui;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.gordoncaleb.chess.engine.legacy.AI;
import com.gordoncaleb.chess.engine.legacy.AISettings;
import com.gordoncaleb.chess.engine.BoardHashEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AISettingsGUI implements MouseListener, KeyListener {
	private static final Logger logger = LoggerFactory.getLogger(AISettingsGUI.class);

	private JTextField version;
	private JCheckBox debugOutput;
	private JCheckBox useLite;
	private JCheckBox useExtraTime;
	private JCheckBox bonusEnable;
	private JTextField maxInCheckFrontierLevel;
	private JTextField maxPieceTakenFrontierLevel;
	private JTextField maxSearchTime;
	private JTextField minSearchDepth;
	private JCheckBox alphaBetaPruningEnabled;
	private JCheckBox useBook;
	private JTextField maxMoveBookMove;
	private JCheckBox useHashTable;
	private JTextField hashIndexSize;
	private JTextField staleHashAge;
	private JTextField numOfThreads;
	private JCheckBox useKillerMove;
	private JTextField maxKillerMoves;

	private JButton clearHashBtn;
	private JButton resetTreeBtn;

	private AI ai;

	private JFrame frame;

	public AISettingsGUI(String title) {
		this(title, null);
	}

	public AISettingsGUI(String title, AI ai) {

		this.ai = ai;

		frame = new JFrame(title);
		
		JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 10));

		JLabel versionLbl = new JLabel("version");
		versionLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		version = new JTextField(AISettings.version);
		version.addMouseListener(this);
		version.addKeyListener(this);
		settingsPanel.add(versionLbl);
		settingsPanel.add(version);

		JLabel debugOutputLbl = new JLabel("");
		debugOutput = new JCheckBox("debugOutput", AISettings.debugOutput);
		debugOutput.addMouseListener(this);
		settingsPanel.add(debugOutputLbl);
		settingsPanel.add(debugOutput);

		JLabel useLiteLbl = new JLabel("");
		useLiteLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useLite = new JCheckBox("useLite", AISettings.useLite);
		useLite.addMouseListener(this);
		settingsPanel.add(useLiteLbl);
		settingsPanel.add(useLite);

		JLabel useExtraTimeLbl = new JLabel("");
		useExtraTimeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useExtraTime = new JCheckBox("useExtraTime", AISettings.useExtraTime);
		useExtraTime.addMouseListener(this);
		settingsPanel.add(useExtraTimeLbl);
		settingsPanel.add(useExtraTime);

		JLabel bonusEnableLbl = new JLabel("");
		bonusEnableLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		bonusEnable = new JCheckBox("bonusEnable", AISettings.bonusEnable);
		bonusEnable.addMouseListener(this);
		settingsPanel.add(bonusEnableLbl);
		settingsPanel.add(bonusEnable);

		JLabel maxInCheckFrontierLevelLbl = new JLabel("maxInCheckFrontierLevel");
		maxInCheckFrontierLevelLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxInCheckFrontierLevel = new JTextField(AISettings.maxInCheckFrontierLevel + "");
		maxInCheckFrontierLevel.addMouseListener(this);
		maxInCheckFrontierLevel.addKeyListener(this);
		settingsPanel.add(maxInCheckFrontierLevelLbl);
		settingsPanel.add(maxInCheckFrontierLevel);

		JLabel maxPieceTakenFrontierLevelLbl = new JLabel("maxPieceTakenFrontierLevel");
		maxPieceTakenFrontierLevelLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxPieceTakenFrontierLevel = new JTextField(AISettings.maxPieceTakenFrontierLevel + "");
		maxPieceTakenFrontierLevel.addMouseListener(this);
		maxPieceTakenFrontierLevel.addKeyListener(this);
		settingsPanel.add(maxPieceTakenFrontierLevelLbl);
		settingsPanel.add(maxPieceTakenFrontierLevel);

		JLabel maxSearchTimeLbl = new JLabel("maxSearchTime");
		maxSearchTimeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxSearchTime = new JTextField(AISettings.maxSearchTime + "");
		maxSearchTime.addMouseListener(this);
		maxSearchTime.addKeyListener(this);
		settingsPanel.add(maxSearchTimeLbl);
		settingsPanel.add(maxSearchTime);

		JLabel minSearchDepthLbl = new JLabel("minSearchDepth");
		minSearchDepthLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		minSearchDepth = new JTextField(AISettings.minSearchDepth + "");
		minSearchDepth.addMouseListener(this);
		minSearchDepth.addKeyListener(this);
		settingsPanel.add(minSearchDepthLbl);
		settingsPanel.add(minSearchDepth);

		JLabel alphaBetaPruningEnabledLbl = new JLabel("");
		alphaBetaPruningEnabledLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		alphaBetaPruningEnabled = new JCheckBox("alphaBetaPruningEnabled", AISettings.alphaBetaPruningEnabled);
		alphaBetaPruningEnabled.addMouseListener(this);
		settingsPanel.add(alphaBetaPruningEnabledLbl);
		settingsPanel.add(alphaBetaPruningEnabled);

		JLabel useBookLbl = new JLabel("");
		useBookLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useBook = new JCheckBox("useBook", AISettings.useBook);
		useBook.addMouseListener(this);
		settingsPanel.add(useBookLbl);
		settingsPanel.add(useBook);

		JLabel maxMoveBookMoveLbl = new JLabel("maxMoveBookMove");
		maxMoveBookMoveLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxMoveBookMove = new JTextField(AISettings.maxMoveBookMove + "");
		maxMoveBookMove.addMouseListener(this);
		settingsPanel.add(maxMoveBookMoveLbl);
		settingsPanel.add(maxMoveBookMove);

		JLabel useHashTableLbl = new JLabel("");
		useHashTableLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useHashTable = new JCheckBox("useHashTable", AISettings.useHashTable);
		useHashTable.addMouseListener(this);
		settingsPanel.add(useHashTableLbl);
		settingsPanel.add(useHashTable);

		JLabel hashIndexSizeLbl = new JLabel("hashIndexSize");
		hashIndexSizeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		hashIndexSize = new JTextField(AISettings.hashIndexSize + "");
		hashIndexSize.addMouseListener(this);
		hashIndexSize.addKeyListener(this);
		settingsPanel.add(hashIndexSizeLbl);
		settingsPanel.add(hashIndexSize);

		JLabel staleHashAgeLbl = new JLabel("staleHashAge");
		staleHashAgeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		staleHashAge = new JTextField(AISettings.staleHashAge + "");
		staleHashAge.addMouseListener(this);
		staleHashAge.addKeyListener(this);
		settingsPanel.add(staleHashAgeLbl);
		settingsPanel.add(staleHashAge);

		JLabel numOfThreadsLbl = new JLabel("numOfThreads");
		numOfThreadsLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		numOfThreads = new JTextField(AISettings.numOfThreads + "");
		numOfThreads.addMouseListener(this);
		numOfThreads.addKeyListener(this);
		settingsPanel.add(numOfThreadsLbl);
		settingsPanel.add(numOfThreads);

		JLabel useKillerMoveLbl = new JLabel("");
		useKillerMoveLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useKillerMove = new JCheckBox("useKillerMove", AISettings.useKillerMove);
		useKillerMove.addMouseListener(this);
		settingsPanel.add(useKillerMoveLbl);
		settingsPanel.add(useKillerMove);

		JLabel maxKillerMovesLbl = new JLabel("maxKillerMoves");
		maxKillerMovesLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxKillerMoves = new JTextField(AISettings.maxKillerMoves + "");
		maxKillerMoves.addMouseListener(this);
		maxKillerMoves.addKeyListener(this);
		settingsPanel.add(maxKillerMovesLbl);
		settingsPanel.add(maxKillerMoves);

		if (ai != null) {
			clearHashBtn = new JButton("Clear Hashtable");
			clearHashBtn.addMouseListener(this);
			settingsPanel.add(clearHashBtn);

			resetTreeBtn = new JButton("Reset Game Tree");
			resetTreeBtn.addMouseListener(this);
			settingsPanel.add(resetTreeBtn);
		}

		// frame.setResizable(false);
		frame.add(settingsPanel);
		frame.setVisible(true);
		frame.pack();

	}

	public void refreshFields() {
		version.setText(AISettings.version);
		debugOutput.setSelected(AISettings.debugOutput);
		useLite.setSelected(AISettings.useLite);
		useExtraTime.setSelected(AISettings.useExtraTime);
		bonusEnable.setSelected(AISettings.bonusEnable);
		maxInCheckFrontierLevel.setText(AISettings.maxInCheckFrontierLevel + "");
		maxPieceTakenFrontierLevel.setText(AISettings.maxPieceTakenFrontierLevel + "");
		maxSearchTime.setText(AISettings.maxSearchTime + "");
		minSearchDepth.setText(AISettings.minSearchDepth + "");
		alphaBetaPruningEnabled.setSelected(AISettings.alphaBetaPruningEnabled);
		useBook.setSelected(AISettings.useBook);
		maxMoveBookMove.setText(AISettings.maxMoveBookMove + "");
		useHashTable.setSelected(AISettings.useHashTable);
		hashIndexSize.setText(AISettings.hashIndexSize + "");
		staleHashAge.setText(AISettings.staleHashAge + "");
		numOfThreads.setText(AISettings.numOfThreads + "");
		useKillerMove.setSelected(AISettings.useKillerMove);
		maxKillerMoves.setText(AISettings.maxKillerMoves + "");
	}

	private void updateHashTableSize() {

		int newSize = Integer.parseInt(hashIndexSize.getText());

		if (newSize != AISettings.hashIndexSize) {
			AISettings.hashIndexSize = newSize;
			AISettings.hashTableSize = (int) Math.pow(2, AISettings.hashIndexSize);
			AISettings.hashIndexMask = (long) (Math.pow(2, AISettings.hashIndexSize) - 1);
			if (ai != null) {
				ai.setHashTable(null);
				System.gc();
				ai.setHashTable(new BoardHashEntry[AISettings.hashTableSize]);
			}
		}
	}

	public void updateSettings() {

		try {
			AISettings.version = version.getText();
			AISettings.debugOutput = debugOutput.isSelected();
			AISettings.useLite = useLite.isSelected();
			AISettings.useExtraTime = useExtraTime.isSelected();
			AISettings.bonusEnable = bonusEnable.isSelected();
			AISettings.maxInCheckFrontierLevel = Integer.parseInt(maxInCheckFrontierLevel.getText());
			AISettings.maxPieceTakenFrontierLevel = Integer.parseInt(maxPieceTakenFrontierLevel.getText());
			AISettings.maxSearchTime = Integer.parseInt(maxSearchTime.getText());
			AISettings.minSearchDepth = Integer.parseInt(minSearchDepth.getText());
			AISettings.alphaBetaPruningEnabled = alphaBetaPruningEnabled.isSelected();
			AISettings.useBook = useBook.isSelected();
			AISettings.maxMoveBookMove = Integer.parseInt(maxMoveBookMove.getText());
			AISettings.useHashTable = useHashTable.isSelected();
			AISettings.staleHashAge = Integer.parseInt(staleHashAge.getText());
			AISettings.numOfThreads = Integer.parseInt(numOfThreads.getText());
			AISettings.useKillerMove = useKillerMove.isSelected();
			AISettings.maxKillerMoves = Integer.parseInt(maxKillerMoves.getText());

			updateHashTableSize();
		} catch (Exception e) {
			logger.debug("Input Error");
		}
	}

	public static void main(String[] args) {
		new AISettingsGUI("Test");
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
		updateSettings();

		if (arg0.getSource() == resetTreeBtn) {
			if (ai != null) {
				ai.resetGameTree();

			}
		}

		if (arg0.getSource() == clearHashBtn) {
			if (ai != null) {
				ai.clearHashTable();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		updateSettings();

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	// public static final String version = "1.2.061112b";
	// public static boolean debugOutput = true;
	// public static final boolean useLite = false;
	// public static final boolean useExtraTime = false;
	// public static final boolean bonusEnable = true;
	// public static final int maxInCheckFrontierLevel = 2;
	// public static final int maxPieceTakenFrontierLevel = 2;
	// public static final long maxSearchTime = 5000;
	// public static final int minSearchDepth = 4;
	// public static final boolean alphaBetaPruningEnabled = true;
	// public static final boolean useBook = true;
	// public static final boolean useHashTable = true;
	// public static final int hashTableSize = (int) Math.pow(2,
	// BoardHashEntry.hashIndexSize);
	// public static final int staleHashAge = 10;
	// public static final int numOfThreads = 1;
	// //Runtime.getRuntime().availableProcessors()
	// public static final boolean useKillerMove = true;
	// public static final int maxKillerMoves = 100;

}
