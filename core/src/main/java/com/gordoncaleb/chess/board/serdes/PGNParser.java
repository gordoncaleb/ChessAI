package com.gordoncaleb.chess.board.serdes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.KING;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.PAWN;
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

    public Map<Long, List<Move>> moveBookFromPGNFile(String fileName) throws Exception {

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

        boolean commenting = false;
        String prevLine = "";
        String line;

        while ((line = lines.readLine()) != null) {

            line = line.trim();

            if (isStartComment(line)) {
                commenting = true;
            }

            if (isEndComment(line)) {
                commenting = false;
                continue;
            }

            if (!commenting) {
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
        }

        return games;
    }

    public boolean isMetaLine(String s) {
        return s.startsWith(METALINE);
    }

    public boolean isStartComment(String s) {
        return s.startsWith("{");
    }

    public boolean isEndComment(String s) {
        return s.startsWith("}");
    }

    public boolean isGameLine(String s) {
        return !s.isEmpty() && !isMetaLine(s);
    }

    public boolean isStartGameLine(String s) {
        return s.startsWith(STARTGAMELINE);
    }

    private boolean isGameStart(String currentLine, String prevLine) {

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

    public Map<Long, Move> getPGNGameAsMoveBook(PGNGame game) throws Exception {

        Map<Long, Move> moveBook = new HashMap<>();
        Board board = getPGNGameAsBoard(game);

        while (board.canUndo()) {
            Move move = board.undoMove();
            moveBook.put(board.getHashCode(), move);
        }

        return moveBook;
    }

    public Board getPGNGameAsBoard(PGNGame game) throws Exception {

        String line = game.gameLine();
        List<String> tokens = Arrays.stream(line.split("[0-9]+\\."))
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .filter(s -> !s.matches("[012/]+-[012/]+"))
                .filter(s -> !s.matches("\\*"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Board board = BoardFactory.getStandardChessBoard();

        for (String notation : tokens) {
            try {
                Move move = resolveAlgebraicNotation(notation, board);
                board.makeMove(move);
            } catch (Exception e) {
                throw new Exception("Error resolving notation " + notation + " board: \n" + board.toJson(true), e);
            }
        }

        return board;
    }

    public Move resolveAlgebraicNotation(String notation, Board board) throws Exception {

        notation = notation.replaceAll("\\+|#", "");

        int fromRow = -1;
        int fromCol = -1;
        int toRow = -1;
        int toCol = -1;

        int note = Move.MoveNote.NONE;
        int pieceMovingID = Piece.PieceID.NONE;

        if (notation.contains("=")) {

            switch (notation.toUpperCase().charAt(notation.indexOf("=") + 1)) {
                case 'Q':
                    note = Move.MoveNote.NEW_QUEEN;
                    break;
                case 'N':
                    note = Move.MoveNote.NEW_KNIGHT;
                    break;
                default:
                    throw new Exception("Unsupported queening option");
            }

            notation = notation.substring(0, notation.indexOf("="));
        }


        if (notation.equals("O-O")) {
            note = Move.MoveNote.CASTLE_NEAR;
        } else {

            if (notation.equals("O-O-O")) {
                note = Move.MoveNote.CASTLE_FAR;
            } else {

                if (notation.length() > 2) {

                    if (notation.length() == 3) {
                        pieceMovingID = charIDtoPieceID(notation.charAt(0));
                        toRow = 7 - (notation.charAt(2) - 49);
                        toCol = notation.charAt(1) - 97;
                    } else {
                        if (notation.contains("x")) {
                            String[] leftRight = notation.split("x");

                            toRow = 7 - (leftRight[1].charAt(1) - 49);
                            toCol = leftRight[1].charAt(0) - 97;

                            pieceMovingID = charIDtoPieceID(leftRight[0].charAt(0));

                            if (pieceMovingID == Piece.PieceID.NONE) {
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

                            pieceMovingID = charIDtoPieceID(notation.charAt(0));

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
                        throw new Exception("Error resolving notation " + notation);
                    }
                }

            }
        }

        return matchValidMove(board, fromRow, fromCol, toRow, toCol, note, pieceMovingID);
    }

    private static int charIDtoPieceID(char id){
        Map<Character,Integer> map = new HashMap<>();
        map.put('R',ROOK);
        map.put('N', KNIGHT);
        map.put('B', BISHOP);
        map.put('Q', QUEEN);
        map.put('K', KING);
        map.put('P', PAWN);
        return Optional.ofNullable(map.get(id)).orElse(NONE);
    }

    public static Optional<Integer> getFileNumberFromAlgNotation(String notation) {
        Pattern p = Pattern.compile("([a-g])([0-9])");
        Matcher m = p.matcher(notation);
        if (m.matches()) {
            String fileLetter = m.group(1);
            return Optional.of(fileLetter.toCharArray()[0] - 'a');
        } else {
            return Optional.empty();
        }
    }

    private Move matchValidMove(Board board, int fromRow, int fromCol, int toRow, int toCol, int note, int pieceMovingID) throws Exception {

        board.makeNullMove();
        List<Move> moves = board.generateValidMoves().toList();

        ArrayList<Move> matchMoves = new ArrayList<>();
        boolean match;
        for (Move move : moves) {

            match = true;

            if (note != Move.MoveNote.NONE) {
                if (move.getNote() != note) {
                    if (move.getNote() == Move.MoveNote.NEW_QUEEN && note == Move.MoveNote.NEW_KNIGHT) {
                        move.setNote(note);
                    } else {
                        match = false;
                    }
                }
            }

            if (fromRow >= 0) {
                if (move.getFromRow() != fromRow) {
                    match = false;
                }
            }

            if (fromCol >= 0) {
                if (move.getFromCol() != fromCol) {
                    match = false;
                }
            }

            if (toCol >= 0) {
                if (move.getToCol() != toCol) {
                    match = false;
                }
            }

            if (toRow >= 0) {
                if (move.getToRow() != toRow) {
                    match = false;
                }
            }

            if (pieceMovingID != Piece.PieceID.NONE) {
                if (board.getPiece(move.getFromRow(), move.getFromCol()).getPieceID() != pieceMovingID) {
                    match = false;
                }
            }

            if (match) {
                matchMoves.add(move);
            }
        }

        if (matchMoves.size() != 1) {
            throw new Exception("No/multiple move matches");
        }

        return matchMoves.get(0);
    }
}
