package com.gordoncaleb.chess.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.groupingBy;

public class MoveBook {
    private static final Logger logger = LoggerFactory.getLogger(MoveBook.class);

    Map<Long, List<Move>> hashMoveBook;

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

    public void removeEntry(Long hashcode, Move move) {
        List<Move> entries = hashMoveBook.get(hashcode);

        if (entries != null) {
            entries.remove(move);

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

        try {
            String jsonMoveBook = JSON.toJSON(hashMoveBook);
            FileIO.writeFile("moveBook.json", jsonMoveBook, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEntry(Long hashcode, Move move) {
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
                    moves.add(Move.fromLong((long) move));
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
