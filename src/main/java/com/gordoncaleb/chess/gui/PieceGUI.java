package com.gordoncaleb.chess.gui;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Piece;

public interface PieceGUI {
	public Piece.PieceID getPieceID();
	public Side getPlayer();
	public void showChessPiece(Piece.PieceID pieceID, Side player);
	public void showAsSelected(boolean selected);
}
