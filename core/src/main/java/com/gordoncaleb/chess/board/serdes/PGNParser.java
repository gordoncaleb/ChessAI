package com.gordoncaleb.chess.board.serdes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.util.JavaLacks.*;
import static java.util.stream.Collectors.*;
import static com.gordoncaleb.chess.board.Move.MoveNote.*;

public class PGNParser {

    private enum AmbiguityLevel {
        NON_AMBIGUOUS, TYPE_DISAMBIGUATE, COL_DISAMBIGUATE, ROW_DISAMBIGUATE, COMPLETE_DISAMBIGUATION;
    }

    private static final Pattern ROWPATTERN = Pattern.compile("[0-9]");
    private static final Pattern COLPATTERN = Pattern.compile("[a-z]");
    private static final Pattern PIECEIDPATTERN = Pattern.compile("[A-Z]");

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

        List<PGNGame> games = loadClassPathFile(fileName);

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

    public static List<PGNGame> loadClassPathFile(String fileName) throws IOException, URISyntaxException {
        try (BufferedReader lines = FileIO.getResourceAsBufferedReader(fileName)) {
            return parseFileLines(lines);
        }
    }

    public static Optional<List<PGNGame>> loadFile(File file) {
        try (BufferedReader lines = Files.newBufferedReader(file.toPath())) {
            return Optional.of(parseFileLines(lines));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static List<PGNGame> parseFileLines(BufferedReader lines) throws IOException {

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

    public static boolean isMetaLine(String s) {
        return s.startsWith(METALINE);
    }

    public static boolean isStartComment(String s) {
        return s.startsWith("{");
    }

    public static boolean isEndComment(String s) {
        return s.startsWith("}");
    }

    public static boolean isGameLine(String s) {
        return !s.isEmpty() && !isMetaLine(s);
    }

    public static boolean isStartGameLine(String s) {
        return s.startsWith(STARTGAMELINE);
    }

    private static boolean isGameStart(String currentLine, String prevLine) {
        return (isMetaLine(currentLine) && isGameLine(prevLine)) ||
                (isStartGameLine(currentLine) && isGameLine(prevLine)) ||
                prevLine.isEmpty();
    }

    public static Map<String, String> parseMetaData(String line) {
        Pattern p = Pattern.compile("\\[(\\w+) \"(.*)\"\\]");
        Matcher m = p.matcher(line);
        if (m.matches()) {
            return ImmutableMap.of(m.group(1), m.group(2));
        } else {
            return Collections.emptyMap();
        }
    }

    public static Map<Long, Move> getPGNGameAsMoveBook(PGNGame game) throws Exception {

        Map<Long, Move> moveBook = new HashMap<>();
        Board board = getPGNGameAsBoard(game);

        while (board.canUndo()) {
            Move move = board.undoMove();
            moveBook.put(board.getHashCode(), move.copy());
        }

        return moveBook;
    }

    public static Board getBoardFromAlgebraicNotation(String gameAN) throws Exception {
        return getBoardFromAlgebraicNotation(gameAN, BoardFactory.getStandardChessBoard());
    }

    public static Board getBoardFromAlgebraicNotation(String gameAN, Board board) throws Exception {
        PGNGame pgnGame = new PGNGame();
        pgnGame.getGameLines().add(gameAN);
        return getPGNGameAsBoard(pgnGame, board);
    }

    public static Board getPGNGameAsBoard(PGNGame game) throws Exception {
        return getPGNGameAsBoard(game, BoardFactory.getStandardChessBoard());
    }

    public static Board getPGNGameAsBoard(PGNGame game, Board board) throws Exception {

        String line = game.gameLine();
        List<String> tokens = Arrays.stream(line.split("[0-9]+\\."))
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .filter(s -> !s.matches("[012/]+-[012/]+"))
                .filter(s -> !s.matches("\\*"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

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

    public static Move resolveAlgebraicNotation(final String notationRaw, Board board) throws Exception {

        //remove unneeded info
        String notation = notationRaw
                .replaceAll("x", "")
                .replaceAll("e\\.p\\.", "")
                .replaceAll("\\+|#", "");

        int note = getMoveNoteFromNotation(notation);

        if (notation.contains("=")) {
            notation = notation.substring(0, notation.indexOf("="));
        }

        final String destAN = destPortionOfAN(notation);
        final String srcAN = srcPortionOfAN(notation);

        final int toRow = rowFromAN(destAN);
        final int toCol = colFromAN(destAN);
        final int pieceMovingID = movingPieceIdFromAN(srcAN);
        final int fromRow = rowFromAN(srcAN);
        final int fromCol = colFromAN(srcAN);

        return matchValidMove(board, fromRow, fromCol, toRow, toCol, note, pieceMovingID);
    }

    private static int rowFromAN(final String an) {
        Matcher m = ROWPATTERN.matcher(an);

        if (m.find()) {
            return 7 - (m.group().charAt(0) - 49);
        } else {
            return -1;
        }
    }

    private static int colFromAN(final String an) {
        Matcher m = COLPATTERN.matcher(an);

        if (m.find()) {
            return m.group().charAt(0) - 97;
        } else {
            return -1;
        }
    }

    private static int movingPieceIdFromAN(final String an) {
        Matcher m = PIECEIDPATTERN.matcher(an);

        if (m.find()) {
            return charIDtoPieceID(m.group().charAt(0));
        } else {
            return Piece.PieceID.PAWN;
        }
    }

    private static String destPortionOfAN(final String an) {
        return an.substring(an.length() - 2);
    }

    private static String srcPortionOfAN(final String an) {
        if (an.length() == 2) {
            return "";
        }
        return an.substring(0, an.length() - 2);
    }

    private static int getMoveNoteFromNotation(String notation) {
        int note = Move.MoveNote.NORMAL;

        if (notation.contains("=")) {

            switch (notation.toUpperCase().charAt(notation.indexOf("=") + 1)) {
                case 'Q':
                    note = Move.MoveNote.NEW_QUEEN;
                    break;
                case 'N':
                    note = Move.MoveNote.NEW_KNIGHT;
                    break;
                case 'B':
                    note = Move.MoveNote.NEW_BISHOP;
                    break;
                case 'R':
                    note = Move.MoveNote.NEW_ROOK;
                    break;
            }

        } else {
            switch (notation.toUpperCase()) {
                case "O-O":
                    note = Move.MoveNote.CASTLE_NEAR;
                    break;
                case "O-O-O":
                    note = Move.MoveNote.CASTLE_FAR;
                    break;
            }
        }

        return note;
    }

    private static int charIDtoPieceID(char id) {
        Map<Character, Integer> map = new HashMap<>();
        map.put('R', ROOK);
        map.put('N', KNIGHT);
        map.put('B', BISHOP);
        map.put('Q', QUEEN);
        map.put('K', KING);
        map.put('P', PAWN);
        return Optional.ofNullable(map.get(id)).orElse(NO_PIECE);
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

    private static Move matchValidMove(Board board,
                                       int fromRow, int fromCol,
                                       int toRow, int toCol,
                                       int note,
                                       int pieceMovingID) throws Exception {

        board.makeNullMove();
        List<Move> moves = board.generateValidMoves().toList();

        ArrayList<Move> matchMoves = new ArrayList<>();
        boolean match;
        for (Move move : moves) {

            match = true;

            if (note != Move.MoveNote.NORMAL && move.getNote() != note) {
                if (move.getNote() == Move.MoveNote.NEW_QUEEN && note == Move.MoveNote.NEW_KNIGHT) {
                    move.setNote(note);
                } else {
                    match = false;
                }
            }

            if (fromRow >= 0 && move.getFromRow() != fromRow) {
                match = false;
            }

            if (fromCol >= 0 && move.getFromCol() != fromCol) {
                match = false;
            }

            if (toCol >= 0 && move.getToCol() != toCol) {
                match = false;
            }

            if (toRow >= 0 && move.getToRow() != toRow) {
                match = false;
            }

            if (pieceMovingID != Piece.PieceID.NO_PIECE) {
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

    public static String toAlgebraicNotation(List<Move> moves, Board b) {

        boolean turnIsBlack = b.getTurn() == Side.BLACK;

        boolean checkMate = false;
        boolean gameOver = false;
        List<String> notations = new ArrayList<>();
        for (Move m : moves) {
            String notation = toAlgebraicNotation(m, b);

            b.makeMove(m);

            b.makeNullMove();

            if (b.isInCheck()) {
                if (b.isCheckMate()) {
                    checkMate = true;
                    notations.add(notation + "#");
                } else {
                    notations.add(notation + "+");
                }
            } else {
                notations.add(notation);
            }

            if (b.isGameOver()) {
                gameOver = true;
                break;
            }
        }

        final int endingTurn = b.getTurn();

        b.undo(notations.size());

        if (turnIsBlack) {
            notations.add(0, "..");
        }

        int moveNumber = 1;
        StringBuilder sb = new StringBuilder();
        while (!notations.isEmpty()) {
            sb.append(moveNumber).append(".");
            if (!notations.isEmpty()) {
                sb.append(notations.remove(0));
            }
            sb.append(" ");
            if (!notations.isEmpty()) {
                sb.append(notations.remove(0));
            }
            sb.append(" ");
            moveNumber++;
        }

        if (gameOver) {
            if (checkMate) {
                sb.append(endingTurn == Side.BLACK ? " 1-0" : " 0-1");
            } else {
                sb.append(" 1/2-1/2");
            }
        }

        return sb.toString();
    }

    public static String toAlgebraicNotation(Move move, Board b) {

        switch (move.getNote()) {
            case NORMAL:
                return normalToAN(move, b);
            case PAWN_LEAP:
                return normalToAN(move, b);
            case EN_PASSANT:
                return normalToAN(move, b) + "e.p";
            case CASTLE_NEAR:
                return "O-O";
            case CASTLE_FAR:
                return "O-O-O";
            default:
                if (move.hasPromotion()) {
                    return normalToAN(move, b) + "=" + Piece.stringID(move.promotionChoice());
                } else {
                    return "????";
                }
        }
    }

    private static String normalToAN(Move move, Board b) {

        String disambiguation = disambiguate(move, b);
        boolean needsDisambiguation = !disambiguation.isEmpty();

        int pieceId = b.getPieceID(move.getFromRow(), move.getFromCol());
        String destinationAN = squareToAN(move.getToRow(), move.getToCol());
        switch (pieceId) {
            case PAWN:
                if (needsDisambiguation) {
                    if (move.hasPieceTaken()) {
                        return disambiguation + "x" + destinationAN;
                    } else {
                        return disambiguation + destinationAN;
                    }
                } else {
                    if (move.hasPieceTaken()) {
                        return colToFileAN(move.getFromCol()) + "x" + destinationAN;
                    } else {
                        return destinationAN;
                    }
                }
            default:
                if (needsDisambiguation) {
                    if (move.hasPieceTaken()) {
                        return disambiguation + "x" + destinationAN;
                    } else {
                        return disambiguation + destinationAN;
                    }
                } else {
                    if (move.hasPieceTaken()) {
                        return Piece.stringID(pieceId) + "x" + destinationAN;
                    } else {
                        return Piece.stringID(pieceId) + destinationAN;
                    }
                }
        }
    }

    public static String squareToAN(int r, int c) {
        return colToFileAN(c) + (8 - r);
    }

    public static String colToFileAN(int c) {
        return Character.toString((char) ('a' + c));
    }

    private static String disambiguate(Move move, Board b) {
        b.makeNullMove();
        AmbiguityLevel al = ambiguityLevel(move, b.generateValidMoves().toList(), b);

        String pieceId = Piece.stringID(b.getPieceID(move.getFromRow(), move.getFromCol()));
        switch (al) {
            case NON_AMBIGUOUS:
                return "";
            case TYPE_DISAMBIGUATE:
                return pieceId;
            case COL_DISAMBIGUATE:
                return pieceId + colToFileAN(move.getFromCol());
            case ROW_DISAMBIGUATE:
                return pieceId + (8 - move.getFromRow());
            case COMPLETE_DISAMBIGUATION:
                return pieceId + squareToAN(move.getFromRow(), move.getFromCol());
            default:
                return "";
        }
    }

    private static AmbiguityLevel ambiguityLevel(Move move, List<Move> moves, Board b) {
        int pieceMovingId = b.getPieceID(move.getFromRow(), move.getFromCol());

        List<Move> ambigMoves = moves.stream()
                .filter(m -> !m.equals(move))
                .filter(m -> m.destinationMatch(move))
                .filter(m -> b.getPieceID(m.getFromRow(), m.getFromCol()) == pieceMovingId)
                .collect(Collectors.toList());

        if (ambigMoves.isEmpty()) {
            return AmbiguityLevel.NON_AMBIGUOUS;
        }

        List<Move> rowAmbigMoves = ambigMoves.stream()
                .filter(m -> m.getFromRow() == move.getFromRow())
                .collect(Collectors.toList());

        List<Move> colAmbigMoves = ambigMoves.stream()
                .filter(m -> m.getFromCol() == move.getFromCol())
                .collect(Collectors.toList());

        if (colAmbigMoves.isEmpty()) {
            return AmbiguityLevel.COL_DISAMBIGUATE;
        } else if (rowAmbigMoves.isEmpty()) {
            return AmbiguityLevel.ROW_DISAMBIGUATE;
        } else {
            return AmbiguityLevel.COMPLETE_DISAMBIGUATION;
        }

    }
}
