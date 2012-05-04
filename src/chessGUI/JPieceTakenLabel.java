package chessGUI;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import chessBackend.Side;
import chessIO.ChessImages;
import chessPieces.PieceID;

public class JPieceTakenLabel extends JLabel implements PieceGUI{
	private PieceID pieceID;
	private Side player;
	
	public JPieceTakenLabel(PieceID pieceID, Side side){
		this.pieceID = pieceID;
		this.player = side;
		
		this.setIcon(ChessImages.getChessIcon(pieceID,side));
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	public PieceID getPieceID() {
		return pieceID;
	}
	
	public Side getPlayer(){
		return player;
	}

	public void showChessPiece(PieceID pieceID, Side player) {
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
