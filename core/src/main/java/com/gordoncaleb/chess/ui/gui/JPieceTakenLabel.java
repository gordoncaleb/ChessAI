package com.gordoncaleb.chess.ui.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class JPieceTakenLabel extends JLabel implements PieceGUI{
	private int pieceID;
	private int player;
	
	public JPieceTakenLabel(int pieceID, int side){
		this.pieceID = pieceID;
		this.player = side;
		
		this.setIcon(ChessImages.getChessIcon(pieceID,side));
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	public int getPieceID() {
		return pieceID;
	}
	
	public int getPlayer(){
		return player;
	}

	public void showChessPiece(int pieceID, int player) {
		this.pieceID = pieceID;
		this.player = player;
		this.setIcon(ChessImages.getChessIcon(pieceID, player));
	}
	
	public void showAsSelected(boolean selected){
		if(selected){
			this.setBorder(BorderFactory.createLineBorder(Color.black));
		}else{
			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		}
	}

}
