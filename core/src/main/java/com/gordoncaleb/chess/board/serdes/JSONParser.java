package com.gordoncaleb.chess.board.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

import java.io.IOException;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.serdes.BoardDTO.*;

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
        BoardDTO boardDTO = JSON.fromJSON(FileIO.readResource(fileName), BoardDTO.class);
        return buildBoard(boardDTO);
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

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setCastle(ImmutableMap.of(Side.toString(Side.WHITE), white,
                Side.toString(Side.BLACK), black));
        boardDTO.setTurn(Side.toString(turn));
        Map<String, String> setupMap = new HashMap<>();
        Arrays.stream(setup).forEach(l -> setupMap.put(8 - setupMap.size() + "", l));
        boardDTO.setSetup(setupMap);
        boardDTO.setHalfMoves(halfMoves);
        boardDTO.setFullMoves(fullMoves);
        boardDTO.setEnPassantFile(enPassantFile);

        return buildBoard(boardDTO);
    }

    public static Board fromJSON(String jsonString) throws IOException {
        BoardDTO boardDTO = JSON.fromJSON(jsonString, BoardDTO.class);
        return JSONParser.buildBoard(boardDTO);
    }

    public static BoardDTO boardToBoardDto(Board board) {
        BoardDTO boardDTO = new BoardDTO();

        CastleRights white = new CastleRights(
                !board.nearRookHasMoved(Side.WHITE) && !board.kingHasMoved(Side.WHITE),
                !board.farRookHasMoved(Side.WHITE) && !board.kingHasMoved(Side.WHITE));

        CastleRights black = new CastleRights(
                !board.nearRookHasMoved(Side.BLACK) && !board.kingHasMoved(Side.BLACK),
                !board.farRookHasMoved(Side.BLACK) && !board.kingHasMoved(Side.BLACK));

        boardDTO.setCastle(ImmutableMap.of(Side.toString(Side.WHITE), white,
                Side.toString(Side.BLACK), black));

        boardDTO.setTurn(Side.toString(board.getTurn()));

        String[] setup = board.toString().split("\n");
        Map<String, String> setupMap = new HashMap<>();
        Arrays.stream(setup).forEach(l -> setupMap.put(8 - setupMap.size() + "", l));
        boardDTO.setSetup(setupMap);

        boardDTO.setHalfMoves(0);
        boardDTO.setFullMoves(0);

        List<Long> moves = board.getMoveHistory().stream()
                .map(m -> m.getMoveLong())
                .collect(Collectors.toList());

        Collections.reverse(moves);
        boardDTO.setMoveHistory(new ArrayList<>(moves));

        Move lastMoveMade = board.getLastMoveMade();
        if (lastMoveMade != null && lastMoveMade.getNote() == Move.MoveNote.ENPASSANT) {
            boardDTO.setEnPassantFile(Optional.of(lastMoveMade.getFromCol()));
        } else {
            boardDTO.setEnPassantFile(Optional.empty());
        }

        return boardDTO;
    }

    public static String toJSON(Board board) throws JsonProcessingException {
        return JSON.toJSON(boardToBoardDto(board));
    }

    public static Board buildBoard(BoardDTO boardDTO) {
        Map<Integer, ArrayList<Piece>> pieces = ImmutableMap.of(Side.WHITE, new ArrayList<>(), Side.BLACK, new ArrayList<>());

        String stringBoard = boardDTO.getSetup().entrySet().stream()
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

        Optional<List<Move>> moveHistory = Optional.ofNullable(boardDTO.getMoveHistory())
                .map(moves -> moves.stream()
                        .map(m -> new Move(m))
                        .collect(Collectors.toList()));

        Board board = new Board(new ArrayList[]{pieces.get(Side.BLACK),
                pieces.get(Side.WHITE)},
                Side.fromString(boardDTO.getTurn()),
                moveHistory.map(ArrayDeque::new)
                        .orElse(new ArrayDeque<>())
        );

        boardDTO.getCastle()
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
