package com.gordoncaleb.chess.io;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gordoncaleb.chess.util.JavaLacks.*;
import static java.util.stream.Collectors.*;

public class PGNParser {
    private static final Logger logger = LoggerFactory.getLogger(PGNParser.class);

    public static class PGNGame {
        Map<String, String> metaData = new HashMap<>();
        List<String> gameLines = new ArrayList<>();

        public List<String> getGameLines() {
            return gameLines;
        }

        public Map<String, String> getMetaData() {
            return metaData;
        }

        public String gameLine() {
            return gameLines.stream().collect(Collectors.joining(" "));
        }

        @Override
        public String toString() {
            return "PGNGame{" +
                    "metaData=" + metaData +
                    ", gameLines=" + gameLines +
                    '}';
        }
    }

    private static final String METALINE = "[";
    private static final String STARTGAMELINE = "1.";

    public Map<Long, List<Long>> moveBookFromPGNFile(String fileName) throws Exception {

        List<PGNGame> games = loadFile(fileName);

        return games.stream()
                .map(g -> {
                    try {
                        return getPGNGameAsMoveBook(g).entrySet();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(s -> s != null)
                .flatMap(Set::stream)
                .collect(
                        groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue,
                                toUniqueList())));
    }

    public List<PGNGame> loadFile(String fileName) throws Exception {
        try (BufferedReader lines = FileIO.getResourceAsBufferedReader(fileName)) {
            return parseFileLines(lines);
        }
    }

    public List<PGNGame> parseFileLines(BufferedReader lines) throws IOException {

        List<PGNGame> games = new ArrayList<>();

        String prevLine = "";
        String line;

        while ((line = lines.readLine()) != null) {

            line = line.trim();

            if (isMetaLine(line) || isGameLine(line)) {

                if (isGameStart(line, prevLine)) {
                    games.add(new PGNGame());
                }

                if (isGameLine(line)) {
                    Iterables.getLast(games).getGameLines().add(line);
                } else if (isMetaLine(line)) {
                    Iterables.getLast(games).getMetaData().putAll(parseMetaData(line));
                }

                prevLine = line;
            }
        }

        return games;
    }

    public boolean isMetaLine(String s) {
        return s.startsWith(METALINE);
    }

    public boolean isGameLine(String s) {
        return !s.isEmpty() && !isMetaLine(s);
    }

    public boolean isStartGameLine(String s) {
        return s.startsWith(STARTGAMELINE);
    }

    public boolean isGameStart(String currentLine, String prevLine) {

        if (isMetaLine(currentLine) && isGameLine(prevLine))
            return true;

        if (isStartGameLine(currentLine) && isGameLine(prevLine))
            return true;

        return prevLine.isEmpty();
    }

    public Map<String, String> parseMetaData(String line) {
        Pattern p = Pattern.compile("\\[(\\w+) \"(.*)\"\\]");
        Matcher m = p.matcher(line);
        if (m.matches()) {
            return ImmutableMap.of(m.group(1), m.group(2));
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<Long, Long> getPGNGameAsMoveBook(PGNGame game) throws Exception {

        Map<Long, Long> moveBook = new HashMap<>();
        Board board = getPGNGameAsBoard(game);

        while (board.canUndo()) {
            long move = board.undoMove();
            moveBook.put(board.getHashCode(), move);
        }

        return moveBook;
    }

    public Board getPGNGameAsBoard(PGNGame game) throws Exception {

        String line = game.gameLine();
        List<String> tokens = Arrays.stream(line.split(" "))
                .map(String::trim)
                .collect(Collectors.toList());

        List<String> notations = getNotations(tokens);
        Board board = BoardFactory.getStandardChessBoard();

        for (String notation : notations) {
            long move = resolveAlgebraicNotation(notation, board);
            board.makeMove(move);
        }

        return board;
    }

    public List<String> getNotations(List<String> tokens) {

        List<String> notations = new ArrayList<>();
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

            if (token.length() > 1) {
                notations.add(token);
            }
        }

        return notations;
    }

    public long resolveAlgebraicNotation(String notation, Board board) throws Exception {

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

        board.makeNullMove();
        ArrayList<Long> moves = board.generateValidMoves();
        ArrayList<Long> matchMoves = new ArrayList<>();
        boolean match;

        for (Long move : moves) {

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
            List<Move> ms = moves.stream().map(Move::new).collect(Collectors.toList());
            logger.error("Could not resolve algebraic notation {} \n{}", notation, board.toXML(true));
            throw new Exception();
        }

        return matchMoves.get(0);
    }
}
