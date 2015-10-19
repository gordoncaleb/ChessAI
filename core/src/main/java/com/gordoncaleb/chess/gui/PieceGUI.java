package com.gordoncaleb.chess.gui;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Piece;

public interface PieceGUI {
    int getPieceID();

    Side getPlayer();

    void showChessPiece(int pieceID, Side player);

    void showAsSelected(boolean selected);
}
