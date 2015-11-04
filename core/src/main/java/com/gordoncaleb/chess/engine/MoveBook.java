package com.gordoncaleb.chess.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.board.serdes.XMLParser;
import com.gordoncaleb.chess.util.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.groupingBy;

public class MoveBook {
    private static final Logger logger = LoggerFactory.getLogger(MoveBook.class);

    Map<Long, List<Move>> hashMoveBook;
    Map<String, List<Move>> verboseMoveBook;

    public static final String MOVEBOOK_FILE = "/movebook/eco.pgn";
    public static final String MOVEBOOK_FILE_COMPILED = "/movebook/eco.bin";

    public static void main(String[] args) {
        MoveBook mb = new MoveBook();
        PGNParser parser = new PGNParser();

        try {
            Map<Long, List<Move>> ecoMb = parser.moveBookFromPGNFile(MOVEBOOK_FILE);
            mb.saveCompiledMoveBook(ecoMb, "./eco.bin");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Move getRecommendation(Long hashCode) {
        Move move = null;

        List<Move> moves = hashMoveBook.get(hashCode);

        if (moves != null && moves.size() > 0) {
            double maxIndex = (double) (moves.size() - 1);
            double randDouble = Math.random();
            double randIndexDouble = randDouble * maxIndex;
            long randIndex = Math.round(randIndexDouble);
            move = moves.get((int) randIndex);
        }

        return move;
    }

    public Optional<List<Move>> getRecommendations(Long hashCode) {
        return Optional.ofNullable(hashMoveBook.get(hashCode));
    }

    public void removeEntry(String xmlBoard, Long hashcode, Move move) {
        List<Move> verboseEntries = verboseMoveBook.get(xmlBoard);
        List<Move> entries = hashMoveBook.get(hashcode);

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

    public Map<Long, List<Move>> loadMoveBook() {
        return loadMoveBook(MOVEBOOK_FILE_COMPILED);
    }

    public Map<Long, List<Move>> loadMoveBook(String fileName) {
        try (DataInputStream din = new DataInputStream(MoveBook.class.getResourceAsStream(fileName))) {
            hashMoveBook = loadCompiledMoveBook(din);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMoveBook;
    }

    public void saveMoveBook() {

        String xmlMoveBook = this.toXML();
        String xmlVerboseMoveBook = this.toVerboseXML();

        FileIO.writeFile("moveBook.xml", xmlMoveBook, false);
        FileIO.writeFile("verboseMoveBook.xml", xmlVerboseMoveBook, false);

    }

    public void addEntry(String xmlBoard, Long hashcode, Move move) {
        List<Move> verboseEntries = verboseMoveBook.get(xmlBoard);
        List<Move> entries = hashMoveBook.get(hashcode);

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
            verboseEntries = new ArrayList<>();
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
        for (List<Move> moves : hashMoveBook.values()) {
            xmlMoveBook += "<entry>\n";

            xmlMoveBook += "<state>\n";

            xmlMoveBook += "<hashcode>";

            xmlMoveBook += Long.toHexString(keys.get(i));

            xmlMoveBook += "</hashcode>\n";

            xmlMoveBook += "</state>\n";

            xmlMoveBook += "<response>\n";

            for (Move m : moves) {
                xmlMoveBook += m.toXML();
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
        for (List<Move> moves : verboseMoveBook.values()) {
            xmlMoveBook += "<entry>\n";

            xmlMoveBook += "<state>\n";

            xmlMoveBook += keys.get(i);

            xmlMoveBook += "</state>\n";

            xmlMoveBook += "<response>\n";

            for (Move m : moves) {
                xmlMoveBook += m.toXML();
            }

            xmlMoveBook += "</response>\n";

            xmlMoveBook += "</entry>\n";
            i++;
        }

        xmlMoveBook += "</moveBook>";
        return xmlMoveBook;
    }

    public void saveCompiledMoveBook(Map<Long, List<Move>> moveBook, String fileName) {

        try (DataOutputStream dout = FileIO.getDataOutputStream(fileName)) {

            for (Map.Entry<Long, List<Move>> entry : moveBook.entrySet()) {
                dout.writeLong(entry.getKey());
                for (Move m : entry.getValue()) {
                    dout.writeShort(m.fromToAsInt());
                }
                dout.writeShort(-1);
            }

            dout.close();

        } catch (IOException e) {
            logger.error("Could not save move book!", e);
        }
    }

    private Map<Long, List<Move>> loadCompiledMoveBook(DataInputStream din) {

        long time1 = System.currentTimeMillis();

        logger.debug("Loading compiled book");

        Map<Long, List<Move>> moveBook = new HashMap<>();

        Long hashCode;
        short move;
        ArrayList<Move> moves;

        boolean eof = false;
        while (!eof) {
            try {
                hashCode = din.readLong();
                moves = new ArrayList<>();

                while ((move = din.readShort()) != -1) {
                    moves.add(new Move((long) move));
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
