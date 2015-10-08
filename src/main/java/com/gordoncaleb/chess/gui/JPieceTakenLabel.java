package com.gordoncaleb.chess.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.io.ChessImages;
import com.gordoncaleb.chess.pieces.Piece;

public class JPieceTakenLabel extends JLabel implements PieceGUI{
	private Piece.PieceID pieceID;
	private Side player;
	
	public JPieceTakenLabel(Piece.PieceID pieceID, Side side){
		this.pieceID = pieceID;
		this.player = side;
		
		this.setIcon(ChessImages.getChessIcon(pieceID,side));
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	public Piece.PieceID getPieceID() {
		return pieceID;
	}
	
	public Side getPlayer(){
		return player;
	}

	public void showChessPiece(Piece.PieceID pieceID, Side player) {
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