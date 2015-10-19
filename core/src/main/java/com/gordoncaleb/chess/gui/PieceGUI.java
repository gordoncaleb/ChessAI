package com.gordoncaleb.chess.gui;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Piece;

public interface PieceGUI {
    int getPieceID();

    int getPlayer();

    void showChessPiece(int pieceID, int player);

    void showAsSelected(boolean selected);
}
