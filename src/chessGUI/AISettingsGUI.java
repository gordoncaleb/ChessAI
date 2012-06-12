package chessGUI;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import chessAI.AI;
import chessAI.AISettings;

public class AISettingsGUI implements MouseListener {

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
	private JCheckBox useHashTable;
	private JTextField hashIndexSize;
	private JTextField staleHashAge;
	private JTextField numOfThreads;
	private JCheckBox useKillerMove;
	private JTextField maxKillerMoves;

	private JButton setAllBtn;
	
	private JButton clearHashBtn;
	private JButton resetTreeBtn;
	
	private AI ai;

	private JFrame frame;

	public AISettingsGUI(String title){
		this(title,null);
	}
	
	public AISettingsGUI(String title, AI ai) {

		this.ai = ai;
		
		frame = new JFrame(title);
		frame.setLayout(new GridLayout(0, 2, 10, 10));

		JLabel versionLbl = new JLabel("version");
		versionLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		version = new JTextField(AISettings.version);
		frame.add(versionLbl);
		frame.add(version);

		JLabel debugOutputLbl = new JLabel("");
		debugOutput = new JCheckBox("debugOutput", AISettings.debugOutput);
		frame.add(debugOutputLbl);
		frame.add(debugOutput);

		JLabel useLiteLbl = new JLabel("");
		useLiteLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useLite = new JCheckBox("useLite", AISettings.useLite);
		frame.add(useLiteLbl);
		frame.add(useLite);

		JLabel useExtraTimeLbl = new JLabel("");
		useExtraTimeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useExtraTime = new JCheckBox("useExtraTime", AISettings.useExtraTime);
		frame.add(useExtraTimeLbl);
		frame.add(useExtraTime);

		JLabel bonusEnableLbl = new JLabel("");
		bonusEnableLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		bonusEnable = new JCheckBox("bonusEnable", AISettings.bonusEnable);
		frame.add(bonusEnableLbl);
		frame.add(bonusEnable);

		JLabel maxInCheckFrontierLevelLbl = new JLabel("maxInCheckFrontierLevel");
		maxInCheckFrontierLevelLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxInCheckFrontierLevel = new JTextField(AISettings.maxInCheckFrontierLevel + "");
		frame.add(maxInCheckFrontierLevelLbl);
		frame.add(maxInCheckFrontierLevel);

		JLabel maxPieceTakenFrontierLevelLbl = new JLabel("maxPieceTakenFrontierLevel");
		maxPieceTakenFrontierLevelLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxPieceTakenFrontierLevel = new JTextField(AISettings.maxPieceTakenFrontierLevel + "");
		frame.add(maxPieceTakenFrontierLevelLbl);
		frame.add(maxPieceTakenFrontierLevel);

		JLabel maxSearchTimeLbl = new JLabel("maxSearchTime");
		maxSearchTimeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxSearchTime = new JTextField(AISettings.maxSearchTime + "");
		frame.add(maxSearchTimeLbl);
		frame.add(maxSearchTime);

		JLabel minSearchDepthLbl = new JLabel("minSearchDepth");
		minSearchDepthLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		minSearchDepth = new JTextField(AISettings.minSearchDepth + "");
		frame.add(minSearchDepthLbl);
		frame.add(minSearchDepth);

		JLabel alphaBetaPruningEnabledLbl = new JLabel("");
		alphaBetaPruningEnabledLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		alphaBetaPruningEnabled = new JCheckBox("alphaBetaPruningEnabled", AISettings.alphaBetaPruningEnabled);
		frame.add(alphaBetaPruningEnabledLbl);
		frame.add(alphaBetaPruningEnabled);

		JLabel useBookLbl = new JLabel("");
		useBookLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useBook = new JCheckBox("useBook", AISettings.useBook);
		frame.add(useBookLbl);
		frame.add(useBook);

		JLabel useHashTableLbl = new JLabel("");
		useHashTableLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useHashTable = new JCheckBox("useHashTable", AISettings.useHashTable);
		frame.add(useHashTableLbl);
		frame.add(useHashTable);

		JLabel hashIndexSizeLbl = new JLabel("hashIndexSize");
		hashIndexSizeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		hashIndexSize = new JTextField(AISettings.hashIndexSize + "");
		frame.add(hashIndexSizeLbl);
		frame.add(hashIndexSize);

		JLabel staleHashAgeLbl = new JLabel("staleHashAge");
		staleHashAgeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		staleHashAge = new JTextField(AISettings.staleHashAge + "");
		frame.add(staleHashAgeLbl);
		frame.add(staleHashAge);

		JLabel numOfThreadsLbl = new JLabel("numOfThreads");
		numOfThreadsLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		numOfThreads = new JTextField(AISettings.numOfThreads + "");
		frame.add(numOfThreadsLbl);
		frame.add(numOfThreads);

		JLabel useKillerMoveLbl = new JLabel("");
		useKillerMoveLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		useKillerMove = new JCheckBox("useKillerMove", AISettings.useKillerMove);
		frame.add(useKillerMoveLbl);
		frame.add(useKillerMove);

		JLabel maxKillerMovesLbl = new JLabel("maxKillerMoves");
		maxKillerMovesLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		maxKillerMoves = new JTextField(AISettings.maxKillerMoves + "");
		frame.add(maxKillerMovesLbl);
		frame.add(maxKillerMoves);

		setAllBtn = new JButton("Apply");
		setAllBtn.addMouseListener(this);
		frame.add(setAllBtn);

		// frame.setResizable(false);
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
		useHashTable.setSelected(AISettings.useHashTable);
		hashIndexSize.setText(AISettings.hashIndexSize + "");
		staleHashAge.setText(AISettings.staleHashAge + "");
		numOfThreads.setText(AISettings.numOfThreads + "");
		useKillerMove.setSelected(AISettings.useKillerMove);
		maxKillerMoves.setText(AISettings.maxKillerMoves + "");
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

		if (arg0.getSource() == setAllBtn) {
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
			AISettings.useHashTable = useHashTable.isSelected();
			AISettings.hashIndexSize = Integer.parseInt(hashIndexSize.getText());
			AISettings.hashTableSize = (int) Math.pow(2, AISettings.hashIndexSize);
			AISettings.staleHashAge = Integer.parseInt(staleHashAge.getText());
			AISettings.numOfThreads = Integer.parseInt(numOfThreads.getText());
			AISettings.useKillerMove = useKillerMove.isSelected();
			AISettings.maxKillerMoves = Integer.parseInt(maxKillerMoves.getText());
			
			refreshFields();
		}

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
