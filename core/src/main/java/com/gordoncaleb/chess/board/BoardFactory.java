package com.gordoncaleb.chess.board;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.board.serdes.BoardDTO;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class BoardFactory {

    private static final Random r = new Random();

    public static Board getStandardChessBoard() {
        return make960Board(1, 2, 2, 1, 2);
    }

    public static Board getRandomChess960Board() {
        return make960Board(rollDie(4), rollDie(4), rollDie(6), rollDie(5), rollDie(4));
    }

    private static Board make960Board(int r1, int r2, int r3, int r4, int r5) {
        ArrayList<Piece>[] pieces = new ArrayList[2];
        pieces[0] = new ArrayList<>();
        pieces[1] = new ArrayList<>();

        int[] pawnRow = new int[2];
        pawnRow[Side.BLACK] = 1;
        pawnRow[Side.WHITE] = 6;

        int[] mainRow = new int[2];
        mainRow[Side.BLACK] = 0;
        mainRow[Side.WHITE] = 7;

        // public Piece(PieceID id, Side player, int row, int col, boolean
        // moved) {
        Piece temp;
        for (int s = 0; s < 2; s++) {
            for (int p = 0; p < 8; p++) {
                temp = new Piece(Piece.PieceID.PAWN, s, pawnRow[s], p);
                pieces[s].add(temp);
            }
        }

        int[] setup = new int[8];
        Arrays.fill(setup, NO_PIECE);

        setup[r1 * 2] = BISHOP;
        setup[r2 * 2 + 1] = BISHOP;

        setup[ithEmptyPosition(r3 + 1, setup)] = QUEEN;

        setup[ithEmptyPosition(r4 + 1, setup)] = KNIGHT;
        setup[ithEmptyPosition(r5 + 1, setup)] = KNIGHT;

        setup[ithEmptyPosition(2, setup)] = KING;

        setup[ithEmptyPosition(1, setup)] = ROOK;
        setup[ithEmptyPosition(1, setup)] = ROOK;

        for (int s = 0; s < 2; s++) {
            for (int p = 0; p < 8; p++) {
                temp = new Piece(setup[p], s, mainRow[s], p);
                pieces[s].add(temp);
            }
        }

        return new Board(pieces[Side.WHITE], pieces[Side.BLACK], Side.WHITE, true, true, true, true);
    }

    private static int ithEmptyPosition(int i, int[] setup) {
        for (int n = 0; n < setup.length; n++) {

            if (setup[n] == NO_PIECE) {
                i--;
            }

            if (i <= 0) {
                return n;
            }
        }

        return setup.length;
    }

    private static int rollDie(int dieSize) {
        return r.nextInt(dieSize);
    }

    public static Board fromFEN(String fen) {
        Pattern p = Pattern.compile("([a-zA-Z0-9\\/]+) ([wb]) ([KQkq]+|[-]) ([a-z][0-9]|[-])\\s*([0-9]*)\\s*([0-9]*)");
        Matcher m = p.matcher(fen.trim());

        if (!m.matches() || m.groupCount() < 3) {
            throw new IllegalArgumentException("FEN is malformed " + fen);
        }

        String setupSection = m.group(1);
        int turn = m.group(2).matches("w") ? Side.WHITE : Side.BLACK;

        String castleSection = m.group(3);
        BoardDTO.CastleRights whiteCastleRights = new BoardDTO.CastleRights(castleSection.contains("K"), castleSection.contains("Q"));
        BoardDTO.CastleRights blackCastleRights = new BoardDTO.CastleRights(castleSection.contains("k"), castleSection.contains("q"));

        Optional<Integer> enPassentFile = PGNParser.getFileNumberFromAlgNotation(m.group(4));

        int halfMoves = Optional.ofNullable(m.group(5))
                .map(s -> s.isEmpty() ? "0" : s)
                .map(Integer::parseInt)
                .orElse(0);

        int fullMoves = Optional.ofNullable(m.group(6))
                .map(s -> s.isEmpty() ? "0" : s)
                .map(Integer::parseInt)
                .orElse(0);

        List<String> setup = Stream.of(setupSection.split("/"))
                .map(BoardFactory::fenLine2JsonLine)
                .collect(Collectors.toList());

        return JSONParser.buildBoard(
                turn,
                setup.toArray(new String[setup.size()]),
                whiteCastleRights,
                blackCastleRights,
                halfMoves,
                fullMoves,
                enPassentFile
        );
    }

    private static String fenLine2JsonLine(String fenLine) {
        return fenLine.chars().mapToObj(c -> Character.toString((char) c))
                .map(s -> {
                    if (s.matches("[0-9]")) {
                        int count = Integer.parseInt(s);
                        return new String(new char[count]).replace("\0", "_,");
                    }
                    return s + ",";
                }).collect(Collectors.joining());
    }

}
