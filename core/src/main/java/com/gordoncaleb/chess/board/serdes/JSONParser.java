package com.gordoncaleb.chess.board.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.serdes.BoardJSON.*;

public class JSONParser {

    private JSONParser() {
    }

    private static final String EMPTY_SPACE = "_";
    private static final Map<String, Integer> pieceIDMap = new ImmutableMap.Builder<String, Integer>()
            .put("R", ROOK)
            .put("N", KNIGHT)
            .put("B", BISHOP)
            .put("Q", QUEEN)
            .put("K", KING)
            .put("P", PAWN)
            .build();

    public static Board getByFileName(String fileName) throws IOException {
        BoardJSON boardJSON = JSON.fromJSON(FileIO.readResource(fileName), BoardJSON.class);
        return buildBoard(boardJSON);
    }

    public static Board getFromSetup(int turn, String[] setup) {
        return buildBoard(turn, setup, CASTLE_RIGHTS, CASTLE_RIGHTS, 0, 0, Optional.empty());
    }

    public static Board buildBoard(int turn,
                                   String[] setup,
                                   CastleRights white,
                                   CastleRights black,
                                   int halfMoves,
                                   int fullMoves,
                                   Optional<Integer> enPassantFile) {

        BoardJSON boardJSON = new BoardJSON();
        boardJSON.setCastle(ImmutableMap.of(Side.toString(Side.WHITE), white,
                Side.toString(Side.BLACK), black));
        boardJSON.setTurn(Side.toString(turn));
        Map<String, String> setupMap = new HashMap<>();
        Arrays.stream(setup).forEach(l -> setupMap.put(8 - setupMap.size() + "", l));
        boardJSON.setSetup(setupMap);
        boardJSON.setHalfMoves(halfMoves);
        boardJSON.setFullMoves(fullMoves);
        boardJSON.setEnPassantFile(enPassantFile);

        return buildBoard(boardJSON);
    }

    public static Board fromJSONFile(File jsonFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        BoardJSON boardJSON = objectMapper.readValue(jsonFile, BoardJSON.class);
        return JSONParser.buildBoard(boardJSON);
    }

    public static Board fromJSON(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        BoardJSON boardJSON = objectMapper.readValue(jsonString, BoardJSON.class);
        return JSONParser.buildBoard(boardJSON);
    }

    public static Board buildBoard(BoardJSON boardJSON) {
        Map<Integer, ArrayList<Piece>> pieces = ImmutableMap.of(Side.WHITE, new ArrayList<>(), Side.BLACK, new ArrayList<>());

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

        Board board = new Board(new ArrayList[]{pieces.get(Side.BLACK),
                pieces.get(Side.WHITE)},
                Side.fromString(boardJSON.getTurn()),
                new ArrayDeque<>()
        );

        boardJSON.getCastle()
                .forEach((side, rights) ->
                        board.applyCastleRights(Side.fromString(side), rights.isNear(), rights.isFar()));

        return board;
    }

    private static Piece piecePieceFromString(String stringPiece, int row, int col) {
        return Optional.ofNullable(pieceIDMap.get(stringPiece.toUpperCase()))
                .map(id -> {
                    int player = stringPiece.matches("[a-z]") ? Side.BLACK : Side.WHITE;
                    return new Piece(id, player, row, col, false);
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid Piece: " + stringPiece));
    }


}
