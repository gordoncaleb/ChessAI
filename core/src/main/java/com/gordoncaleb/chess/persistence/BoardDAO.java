package com.gordoncaleb.chess.persistence;

import com.google.common.collect.ImmutableMap;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.io.FileIO;
import com.gordoncaleb.chess.io.XMLParser;
import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.pieces.Piece.*;

public class BoardDAO {

    private static final String EMPTY_SPACE = "_";
    private static final Map<String, Integer> pieceIDMap = new ImmutableMap.Builder<String, Integer>()
            .put("R", ROOK)
            .put("N", KNIGHT)
            .put("B", BISHOP)
            .put("Q", QUEEN)
            .put("K", KING)
            .put("P", PAWN)
            .build();

    public Board getByFileName(String fileName) throws IOException {
        BoardJSON boardJSON = JSON.fromJSON(FileIO.readResource(fileName), BoardJSON.class);
        return buildBoard(boardJSON);
    }

    public Board getByXMLFileName(String fileName) {
        return XMLParser.XMLToBoard(FileIO.readResource(fileName));
    }

    public Board getFromSetup(Side turn, String[] setup) {
        BoardJSON boardJSON = new BoardJSON();
        boardJSON.setCastle(ImmutableMap.of(Side.WHITE, BoardJSON.NO_CASTLE_RIGHTS, Side.BLACK, BoardJSON.NO_CASTLE_RIGHTS));
        boardJSON.setTurn(turn);

        Map<String, String> setupMap = new HashMap<>();
        Arrays.stream(setup).forEach(l -> setupMap.put(8 - setupMap.size() + "", l));
        boardJSON.setSetup(setupMap);

        return buildBoard(boardJSON);
    }

    private Board buildBoard(BoardJSON boardJSON) {
        Map<Side, ArrayList<Piece>> pieces = ImmutableMap.of(Side.WHITE, new ArrayList<>(), Side.BLACK, new ArrayList<>());

        String stringBoard = boardJSON.getSetup().entrySet().stream()
                .sorted((b, a) -> a.getKey().compareTo(b.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining());

        String[] stringPieces = stringBoard.split(",");
        Piece piece;
        String stringPiece;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                stringPiece = stringPieces[8 * row + col].trim();

                if (!stringPiece.equals(EMPTY_SPACE)) {
                    piece = piecePieceFromString(stringPiece, row, col);
                    pieces.get(piece.getSide()).add(piece);
                }
            }
        }

        Board board = new Board(new ArrayList[]{pieces.get(Side.BLACK), pieces.get(Side.WHITE)}, boardJSON.getTurn(), new Stack<>(), null, null);

        boardJSON.getCastle()
                .forEach((side, rights) ->
                        board.applyCastleRights(side, rights.isNear(), rights.isFar()));

        return board;
    }

    private Piece piecePieceFromString(String stringPiece, int row, int col) {
        return Optional.ofNullable(pieceIDMap.get(stringPiece.toUpperCase()))
                .map(id -> {
                    Side player = stringPiece.matches("[a-z]") ? Side.WHITE : Side.BLACK;
                    return new Piece(id, player, row, col, false);
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid Piece: " + stringPiece));
    }


}
