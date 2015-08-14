package com.gordoncaleb.chess.io;

import com.google.common.collect.Iterables;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PGNParser {
    private static final Logger logger = LoggerFactory.getLogger(PGNParser.class);

    public static Map<Long, List<Long>> moveBookFromPGNFile(String fileName) {

        long time1 = System.currentTimeMillis();
        Map<Long, List<Long>> moveBook = new HashMap<>();

        try (Stream<String> lines = FileIO.readFileStream(fileName)) {

            List<List<String>> games = new ArrayList<>();

            lines.map(String::trim)
                    .filter(line -> !line.startsWith("[") && !line.isEmpty())
                    .forEach(line -> {
                        if (line.startsWith("1")) {
                            games.add(new ArrayList<>());
                        }
                        Iterables.getLast(games).add(line);
                    });

            List<String> gameLines = games.stream()
                    .map(ls -> ls.stream().collect(Collectors.joining(" ")))
                    .collect(Collectors.toList());

            logger.debug(games.size() + " games loaded");

            for (String line : gameLines) {
                moveBook.putAll(processGamePGNLine(line));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("Move book loaded!!!!");
        logger.debug("Took " + (System.currentTimeMillis() - time1) + "ms to load");
        logger.debug("Hash Count = " + moveBook.size());

        return moveBook;
    }

    private static Map<Long, List<Long>> processGamePGNLine(String line) {

        Map<Long, List<Long>> moveBook = new HashMap<>();
        Board board = BoardFactory.getStandardChessBoard();

        List<String> notations = new ArrayList<>();
        List<String> tokens;

        tokens = Arrays.stream(line.split(" "))
                .map(String::trim)
                .collect(Collectors.toList());

        for (String token : tokens) {

            if (token.contains(".")) {
                token = token.substring(token.indexOf(".") + 1);
            }

            if (token.contains("O-O-O")) {
                token = "O-O-O";
            } else {
                if (token.contains("O-O")) {
                    token = "O-O";
                }
            }

            if (!token.equals("O-O") && !token.equals("O-O-O")) {
                while (token.length() > 1) {
                    if (!Character.isLowerCase(token.charAt(token.length() - 2)) || !Character.isDigit(token.charAt(token.length() - 1))) {
                        token = token.substring(0, token.length() - 1);
                    } else {
                        break;
                    }
                }
            }

            if (!token.equals("") && token.length() > 1) {
                notations.add(token);
            }
        }

        long move;
        List<Long> moves;
        for (String notation : notations) {

            move = resolveAlgebraicNotation(notation, board);

            moves = moveBook.get(board.getHashCode());
            if (moves != null) {
                if (!moves.contains(move)) {
                    moves.add(move);
                }
            } else {
                moves = new ArrayList<>();
                moves.add(move);
                moveBook.put(board.getHashCode(), moves);
            }

            board.makeMove(move);
        }

        return moveBook;
    }

    public static long resolveAlgebraicNotation(String notation, Board board) {

        board.makeNullMove();
        ArrayList<Long> moves = board.generateValidMoves();

        int fromRow = -1;
        int fromCol = -1;
        int toRow = -1;
        int toCol = -1;

        Move.MoveNote note = null;
        Piece.PieceID pieceMovingID = null;
        boolean pieceTaken = false;

        if (notation.equals("O-O")) {
            note = Move.MoveNote.CASTLE_NEAR;
        } else {

            if (notation.equals("O-O-O")) {
                note = Move.MoveNote.CASTLE_FAR;
            } else {

                if (notation.length() > 2) {

                    if (notation.length() == 3) {
                        pieceMovingID = Piece.charIDtoPieceID(notation.charAt(0));
                        toRow = 7 - (notation.charAt(2) - 49);
                        toCol = notation.charAt(1) - 97;
                    } else {
                        if (notation.contains("x")) {
                            pieceTaken = true;
                            String[] leftRight = notation.split("x");

                            toRow = 7 - (leftRight[1].charAt(1) - 49);
                            toCol = leftRight[1].charAt(0) - 97;

                            pieceMovingID = Piece.charIDtoPieceID(leftRight[0].charAt(0));

                            if (pieceMovingID == null) {
                                pieceMovingID = Piece.PieceID.PAWN;
                                fromCol = leftRight[0].charAt(0) - 97;
                            }

                            if (leftRight[0].length() > 1) {
                                if (leftRight[0].charAt(1) >= 97) {
                                    fromCol = leftRight[0].charAt(1) - 97;
                                } else {
                                    fromRow = 7 - (leftRight[0].charAt(1) - 49);
                                }
                            }
                        } else {
                            toRow = 7 - (notation.charAt(notation.length() - 1) - 49);
                            toCol = notation.charAt(notation.length() - 2) - 97;

                            pieceMovingID = Piece.charIDtoPieceID(notation.charAt(0));

                            if (notation.charAt(1) >= 97) {
                                fromCol = notation.charAt(1) - 97;
                            } else {
                                fromRow = 7 - (notation.charAt(1) - 49);
                            }

                        }
                    }

                } else {
                    if (notation.length() == 2) {
                        toRow = 7 - (notation.charAt(1) - 49);
                        toCol = notation.charAt(0) - 97;
                        pieceMovingID = Piece.PieceID.PAWN;
                    } else {
                        logger.debug("Error resolving notation " + notation);
                    }
                }

            }
        }

        ArrayList<Long> matchMoves = new ArrayList<>();
        boolean match;

        for (Long move: moves) {

            match = true;

            if (note != null) {
                if (Move.getNote(move) != note) {
                    match = false;
                }
            }

            if (fromRow >= 0) {
                if (Move.getFromRow(move) != fromRow) {
                    match = false;
                }
            }

            if (fromCol >= 0) {
                if (Move.getFromCol(move) != fromCol) {
                    match = false;
                }
            }

            if (toCol >= 0) {
                if (Move.getToCol(move) != toCol) {
                    match = false;
                }
            }

            if (toRow >= 0) {
                if (Move.getToRow(move) != toRow) {
                    match = false;
                }
            }

            if (pieceMovingID != null) {
                if (board.getPiece(Move.getFromRow(move), Move.getFromCol(move)).getPieceID() != pieceMovingID) {
                    match = false;
                }
            }

            if (match) {
                matchMoves.add(move);
            }
        }

        if (matchMoves.size() != 1) {
            ArrayList<Move> movesDetailed = new ArrayList<>();
            for (int i = 0; i < moves.size(); i++) {
                movesDetailed.add(new Move(moves.get(i)));
            }
            logger.debug("ERROR resolving algebraic notation " + notation);
            return 0;
        }

        return matchMoves.get(0);
    }
}
