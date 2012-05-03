package chessGUI;

import chessBackend.Side;
import chessPieces.PieceID;

public interface PieceGUI {
	public PieceID getPieceID();
	public Side getPlayer();
	public void showChessPiece(PieceID pieceID, Side player);
	public void showAsSelected(boolean selected);
}
