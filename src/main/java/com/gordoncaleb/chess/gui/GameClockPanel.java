package com.gordoncaleb.chess.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.gordoncaleb.chess.backend.GameClock;

public class GameClockPanel extends JPanel{
	
	private JPanel whiteClockPanel;
	private JLabel whiteClockLbl;
	
	private JPanel blackClockPanel;
	private JLabel blackClockLbl;
	
	private GameClock gameClock;
	
	
	public GameClockPanel(GameClock gameClock){
		this.setLayout(new BorderLayout());
		this.gameClock = gameClock;
		
		whiteClockPanel = new JPanel();
		whiteClockLbl = new JLabel();
		whiteClockPanel.add(whiteClockLbl);
		
		blackClockPanel = new JPanel();
		blackClockLbl = new JLabel();
		blackClockPanel.add(blackClockLbl);
		
	}
	
	public void refresh(){
		
	}
	
	
}
