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

    Map<Long, List<Long>> hashMoveBook;
    Map<String, List<Long>> verboseMoveBook;

    public static final String MOVEBOOK_FILE = "/movebook/eco.pgn";
    public static final String MOVEBOOK_FILE_COMPILED = "/movebook/eco.bin";

    public static void main(String[] args) {
        MoveBook mb = new MoveBook();
        PGNParser parser = new PGNParser();

        try {
            Map<Long, List<Long>> ecoMb = parser.moveBookFromPGNFile(MOVEBOOK_FILE);
            mb.saveCompiledMoveBook(ecoMb, "./eco.bin");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    public Optional<List<Long>> getRecommendations(Long hashCode) {
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
        return loadMoveBook(MOVEBOOK_FILE_COMPILED);
    }

    public Map<Long, List<Long>> loadMoveBook(String fileName) {
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

    public void saveCompiledMoveBook(Map<Long, List<Long>> moveBook, String fileName) {

        try (DataOutputStream dout = FileIO.getDataOutputStream(fileName)) {

            for (Map.Entry<Long, List<Long>> entry : moveBook.entrySet()) {
                dout.writeLong(entry.getKey());
                for (Long m : entry.getValue()) {
                    dout.writeShort((int) (Move.fromToMask & m));
                }
                dout.writeShort(-1);
            }

            dout.close();

        } catch (IOException e) {
            logger.error("Could not save move book!", e);
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
