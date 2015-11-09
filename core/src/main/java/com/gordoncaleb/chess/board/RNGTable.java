package com.gordoncaleb.chess.board;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.gordoncaleb.chess.util.FileIO;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RNGTable {
    private static final Logger logger = LoggerFactory.getLogger(RNGTable.class);

    public static final int YES = 1;
    public static final int NO = 0;

    public static final String LOAD_FROM_FILE = "/doc/random.txt";
    public static final int RANDOM_COUNT = 793;

    private final Stack<Long> rng = new Stack<>();
    private List<Long> loadedRandoms;

    //private static final byte[] seed = {-52, 45, -101, 26, -51, -99, -84, -79};
    private static final byte[] seed = {119, -7, 118, 69, 49, -56, -94, 117, -11, 27, -52, -50, -103, 84, 19, 111, 85, 72, 115, -115};

    public static RNGTable instance = new RNGTable();

    private long[][][][] piecePerSquare;
    private long blackToMove;
    private long[][][] castlingRights;
    private long[] enPassantFile;

    public static void main(String[] args) {

        String contents = RNGTable.instance.getLoadedRandoms().stream()
                .map(l -> Long.toHexString(l))
                .collect(Collectors.joining("\n"));

        FileIO.writeFile("./random.txt", contents, false);
        logger.info("Random file written");
    }

    public RNGTable() {
        this(LOAD_FROM_FILE);
    }

    public RNGTable(String fileName) {

        try {
            loadedRandoms = loadFromFile(fileName);
        } catch (Exception e) {
            loadedRandoms = generateTable();
        }

        loadedRandoms.stream().forEach(rng::push);

        generatePiecePerSquare();
        generateBlackToMove();
        generateCastlingRights();
        generateEnPassantFile();
    }

    public List<Long> loadFromFile(String fileName) throws Exception {
        List<String> randomLines = Files.readAllLines(Paths.get(RNGTable.class.getResource(fileName).toURI()));
        return randomLines.stream().map(s -> new BigInteger(s, 16).longValue()).collect(Collectors.toList());
    }

    private List<Long> generateTable() {
        SecureRandom r = new SecureRandom(seed);
        List<Long> loadedRandoms = new ArrayList<>();
        for (int i = 0; i < RANDOM_COUNT; i++) {
            loadedRandoms.add(r.nextLong());
        }
        return loadedRandoms;
    }

    private long nextLong() {
        return rng.pop();
    }

    private void generatePiecePerSquare() {
        int numPieceType = Piece.PieceID.PIECES_COUNT;

        piecePerSquare = new long[2][numPieceType][8][8];

        for (int player = 0; player < 2; player++) {
            for (int pieceType = 0; pieceType < numPieceType; pieceType++) {
                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {
                        piecePerSquare[player][pieceType][r][c] = nextLong();
                    }
                }
            }
        }
    }

    public long getPiecePerSquareRandom(int player, int id, int row, int col) {
        return piecePerSquare[player][id][row][col];
    }

    private void generateBlackToMove() {
        blackToMove = nextLong();
    }

    public long getBlackToMoveRandom() {
        return blackToMove;
    }

    private void generateCastlingRights() {
        castlingRights = new long[2][2][2];

        for (int s : Arrays.asList(Side.BLACK, Side.WHITE)) {
            for (int near : Arrays.asList(NO, YES)) {
                for (int far : Arrays.asList(NO, YES)) {
                    castlingRights[s][near][far] = nextLong();
                }
            }
        }

    }

    public long getCastlingRightsRandom(int side,
                                        int canNear,
                                        int canFar) {
        return castlingRights[side][canNear][canFar];
    }

    private void generateEnPassantFile() {
        enPassantFile = new long[8];
        for (int f = 0; f < 8; f++) {
            enPassantFile[f] = nextLong();
        }
    }

    public long getEnPassantFile(int file) {
        return enPassantFile[file];
    }

    public List<Long> getLoadedRandoms() {
        return loadedRandoms;
    }
}
