package com.gordoncaleb.chess.board.persistence;

import com.google.common.collect.ImmutableMap;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

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

    public Board getFromSetup(int turn, String[] setup) {
        return getFromSetup(turn, setup, BoardJSON.CASTLE_RIGHTS, BoardJSON.CASTLE_RIGHTS);
    }

    public Board getFromSetup(int turn, String[] setup, BoardJSON.CastleRights white, BoardJSON.CastleRights black) {
        BoardJSON boardJSON = new BoardJSON();
        boardJSON.setCastle(ImmutableMap.of(Side.toString(Side.WHITE), white,
                Side.toString(Side.BLACK), black));

        boardJSON.setTurn(Side.toString(turn));

        Map<String, String> setupMap = new HashMap<>();
        Arrays.stream(setup).forEach(l -> setupMap.put(8 - setupMap.size() + "", l));
        boardJSON.setSetup(setupMap);

        return buildBoard(boardJSON);
    }

    private Board buildBoard(BoardJSON boardJSON) {
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
                new ArrayDeque<>(),
                null,
                null
        );

        boardJSON.getCastle()
                .forEach((side, rights) ->
                        board.applyCastleRights(Side.fromString(side), rights.isNear(), rights.isFar()));

        return board;
    }

    private Piece piecePieceFromString(String stringPiece, int row, int col) {
        return Optional.ofNullable(pieceIDMap.get(stringPiece.toUpperCase()))
                .map(id -> {
                    int player = stringPiece.matches("[a-z]") ? Side.BLACK : Side.WHITE;
                    return new Piece(id, player, row, col, false);
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid Piece: " + stringPiece));
    }

    public Board fromFEN(String fen) {
        Pattern p = Pattern.compile("([a-zA-Z0-9\\/]+) ([wb]) ([KQkq]+|[-]) ([a-z][0-9]|[-])\\s?([0-9]?)\\s?([0-9]?)");
        Matcher m = p.matcher(fen.trim());

        if (!m.matches() || m.groupCount() < 3) {
            throw new IllegalArgumentException("FEN is malformed " + fen);
        }

        String setupSection = m.group(1);
        int turn = m.group(2).matches("w") ? Side.WHITE : Side.BLACK;

        String castleSection = m.group(3);
        BoardJSON.CastleRights whiteCastleRights = new BoardJSON.CastleRights(castleSection.contains("K"), castleSection.contains("Q"));
        BoardJSON.CastleRights blackCastleRights = new BoardJSON.CastleRights(castleSection.contains("k"), castleSection.contains("q"));

        String enPassentFile = Optional.ofNullable(m.group(4)).orElse("-");

        int halfMoves = Optional.ofNullable(m.group(5))
                .map(s -> s.isEmpty() ? "0" : s)
                .map(Integer::parseInt)
                .orElse(0);

        int fullMoves = Optional.ofNullable(m.group(6))
                .map(s -> s.isEmpty() ? "0" : s)
                .map(Integer::parseInt)
                .orElse(0);

        List<String> setup = Stream.of(setupSection.split("/"))
                .map(this::fen2Json)
                .collect(Collectors.toList());

        return getFromSetup(turn, setup.toArray(new String[setup.size()]), whiteCastleRights, blackCastleRights);
    }

    private String fen2Json(String fenLine) {

        return fenLine.chars().mapToObj(c -> Character.toString((char) c))
                .map(s -> {
                    if (s.matches("[0-9]")) {
                        int count = Integer.parseInt(s);
                        return new String(new char[count]).replace("\0", "_,");
                    }
                    return s + ",";
                }).collect(Collectors.joining());
    }


}
