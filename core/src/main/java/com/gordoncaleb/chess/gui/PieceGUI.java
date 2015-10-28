package com.gordoncaleb.chess.gui;

public interface PieceGUI {
    int getPieceID();

    int getPlayer();

    void showChessPiece(int pieceID, int player);

    void showAsSelected(boolean selected);
}
