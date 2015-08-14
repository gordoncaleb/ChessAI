package com.gordoncaleb.chess.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.backend.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveBook {
    private static final Logger logger = LoggerFactory.getLogger(MoveBook.class);

    Map<Long, List<Long>> hashMoveBook;
    Map<String, List<Long>> verboseMoveBook;

    public static final String MOVEBOOK_COMPILED = "/doc/book.pgn.compiled";


    public long getRecommendation(Long hashCode) {
        long move = 0;

        List<Long> moves = hashMoveBook.get(hashCode);

        if (moves != null && moves.size() > 0) {
            double maxIndex = (double) (moves.size() - 1);
            double randDouble = Math.random();
            double randIndexDouble = randDouble * maxIndex;
            long randIndex = Math.round(randIndexDouble);
            move = moves.get((int) randIndex);
        }

        return move;
    }

    public Optional<List<Long>> getAllRecommendations(Long hashCode) {
        return Optional.ofNullable(hashMoveBook.get(hashCode));
    }

    public void removeEntry(String xmlBoard, Long hashcode, Move move) {
        List<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
        List<Long> entries = hashMoveBook.get(hashcode);

        if (verboseEntries != null) {
            verboseEntries.remove(move.getMoveLong());

            if (verboseEntries.size() == 0) {
                verboseMoveBook.remove(xmlBoard);
            }
        }

        if (entries != null) {
            entries.remove(move.getMoveLong());

            if (entries.size() == 0) {
                hashMoveBook.remove(hashcode);
            }
        }
    }

    public Map<Long, List<Long>> loadMoveBook() {
        String fileName = MOVEBOOK_COMPILED;
        try (DataInputStream din = new DataInputStream(MoveBook.class.getResourceAsStream(fileName))) {
            hashMoveBook = loadCompiledMoveBook(din);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMoveBook;
    }

    private void loadVerboseMoveBook() {
        verboseMoveBook = XMLParser.XMLToVerboseMoveBook(FileIO.readFile("verboseMoveBook.xml"));
        loadMoveBook();
    }

    public void saveMoveBook() {

        String xmlMoveBook = this.toXML();
        String xmlVerboseMoveBook = this.toVerboseXML();

        FileIO.writeFile("moveBook.xml", xmlMoveBook, false);
        FileIO.writeFile("verboseMoveBook.xml", xmlVerboseMoveBook, false);

    }

    public void addEntry(String xmlBoard, Long hashcode, long move) {
        List<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
        List<Long> entries = hashMoveBook.get(hashcode);

        if (entries != null) {
            if (!entries.contains(move)) {
                entries.add(move);
            } else {
                return;
            }
        } else {
            entries = new ArrayList<>();
            entries.add(move);
            hashMoveBook.put(hashcode, entries);
        }

        if (verboseEntries != null) {
            verboseEntries.add(move);
        } else {
            verboseEntries = new ArrayList<Long>();
            verboseEntries.add(move);
            verboseMoveBook.put(xmlBoard, verboseEntries);
        }

    }

    public String toXML() {
        if (hashMoveBook == null) {
            return "";
        }

        String xmlMoveBook = "<moveBook>\n";

        int i = 0;
        ArrayList<Long> keys = new ArrayList<Long>(hashMoveBook.keySet());
        for (List<Long> moves : hashMoveBook.values()) {
            xmlMoveBook += "<entry>\n";

            xmlMoveBook += "<state>\n";

            xmlMoveBook += "<hashcode>";

            xmlMoveBook += Long.toHexString(keys.get(i));

            xmlMoveBook += "</hashcode>\n";

            xmlMoveBook += "</state>\n";

            xmlMoveBook += "<response>\n";

            for (Long m : moves) {
                xmlMoveBook += Move.toXML(m);
            }

            xmlMoveBook += "</response>\n";

            xmlMoveBook += "</entry>\n";
            i++;
        }

        xmlMoveBook += "</moveBook>";
        return xmlMoveBook;
    }

    public String toVerboseXML() {

        if (verboseMoveBook == null) {
            return "";
        }

        String xmlMoveBook = "<moveBook>\n";

        int i = 0;
        List<String> keys = new ArrayList<>(verboseMoveBook.keySet());
        for (List<Long> moves : verboseMoveBook.values()) {
            xmlMoveBook += "<entry>\n";

            xmlMoveBook += "<state>\n";

            xmlMoveBook += keys.get(i);

            xmlMoveBook += "</state>\n";

            xmlMoveBook += "<response>\n";

            for (Long m : moves) {
                xmlMoveBook += Move.toXML(m);
            }

            xmlMoveBook += "</response>\n";

            xmlMoveBook += "</entry>\n";
            i++;
        }

        xmlMoveBook += "</moveBook>";
        return xmlMoveBook;
    }

    public static Map<Long, List<Long>> moveBookFromPGNFile(String fileName) {

        int moveCount = 0;
        long time1 = System.currentTimeMillis();
        Map<Long, List<Long>> moveBook = new HashMap<>();

        String contents = FileIO.readFile(fileName);

        String[] lines = contents.split("\n");

        List<String> gameLines = new ArrayList<>();
        String gameLine = "";
        boolean gameLineStarted = false;

        for (String line: lines) {
            if (!line.trim().startsWith("[") && !line.trim().equals("")) {
                gameLine += line.trim();
                gameLineStarted = true;
            } else {
                if (gameLineStarted) {
                    gameLines.add(gameLine);
                    gameLine = "";
                }
                gameLineStarted = false;
            }
        }

        if (gameLineStarted) {
            gameLines.add(gameLine);
        }

        logger.debug(gameLines.size() + " game lines");

        Board board = BoardFactory.getStandardChessBoard();

        List<String> notations = new ArrayList<>();
        String[] tokens;
        String token;
        for (int i = 0; i < gameLines.size(); i++) {

            // logger.debug("NEW GAME " + gameLines.get(i));
            tokens = gameLines.get(i).split(" ");

            for (int n = 0; n < tokens.length; n++) {

                token = tokens[n].trim();

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
            for (int n = 0; n < notations.size(); n++) {

                move = board.resolveAlgebraicNotation(notations.get(n));

                // logger.debug(notations.get(n) + " => " + (new
                // Move(move)));

                moves = moveBook.get(board.getHashCode());
                if (moves != null) {
                    if (!moves.contains(move)) {
                        moves.add(move);
                        moveCount++;
                    }
                } else {
                    moves = new ArrayList<>();
                    moves.add(move);
                    moveCount++;

                    moveBook.put(board.getHashCode(), moves);
                }

                board.makeMove(move);
            }

            while (board.canUndo()) {
                board.undoMove();
            }

            notations.clear();

        }

        logger.debug("Move book loaded!!!!");
        logger.debug("Move count " + moveCount);
        logger.debug("Took " + (System.currentTimeMillis() - time1) + "ms to load");
        logger.debug("Hash Count = " + moveBook.size());

        saveCompiledMoveBook(moveBook);

        return moveBook;
    }

    public static void saveCompiledMoveBook(Map<Long, List<Long>> moveBook) {

        DataOutputStream dout = FileIO.getDataOutputStream(MOVEBOOK_COMPILED);

        logger.debug("Creating compiled movebook file");
        try {

            for (Map.Entry<Long, List<Long>> entry : moveBook.entrySet()) {
                dout.writeLong(entry.getKey());
                for (Long m : entry.getValue()) {
                    dout.writeShort((int) (Move.fromToMask & m));
                }
                dout.writeShort(-1);
            }

            logger.debug("Done!");

            dout.close();

        } catch (IOException e) {
            logger.debug("File io exception");
        }
    }

    private Map<Long, List<Long>> loadCompiledMoveBook(DataInputStream din) {

        long time1 = System.currentTimeMillis();

        logger.debug("Loading compiled book");

        Map<Long, List<Long>> moveBook = new HashMap<>();

        Long hashCode;
        short move;
        ArrayList<Long> moves;

        boolean eof = false;
        while (!eof) {
            try {
                hashCode = din.readLong();
                moves = new ArrayList<>();

                while ((move = din.readShort()) != -1) {
                    moves.add((long) move);
                }

                moveBook.put(hashCode, moves);

            } catch (EOFException e) {
                eof = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            din.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.debug("Done");

        logger.debug("Compiled Book took " + (System.currentTimeMillis() - time1) + "ms  to load");

        return moveBook;
    }

}
