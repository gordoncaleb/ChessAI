package com.gordoncaleb.chess.gui;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.PieceID;

public interface PieceGUI {
	public PieceID getPieceID();
	public Side getPlayer();
	public void showChessPiece(PieceID pieceID, Side player);
	public void showAsSelected(boolean selected);
}
