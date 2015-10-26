package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.persistence.BoardDAO;

public class Perft {

    public Board standardInitialPosition() {
        return BoardFactory.getStandardChessBoard();
    }

    public Board kiwiPetePosition() {
        String[] setup = new String[]{
                "r_,_,_,k,_,_r",
                "p1ppqpb1",
                "bn2pnp1",
                "3PN3",
                "1p2P3",
                "2N2Q1p",
                "PPPBBPPP",
                "R3K2R"
        };
        return new BoardDAO().getFromSetup(Side.WHITE, setup);
    }
}
