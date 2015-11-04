package com.gordoncaleb.chess.board.bitboard;

import com.gordoncaleb.chess.board.Move;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BitBoardTest {

    @Test
    public void testBitBoardToMoves() {

        String[] bbString = new String[]{
                "1,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        List<Integer> solution = Arrays.asList(
                0, 0,
                0, 7,
                4, 0,
                7, 7
        );

        verifyBitBoardToMoves(bbString, solution);
    }

    private void bitBoardToMoves(int fromRow, int fromCol, long bb, List<Move> moves) {
        BitBoard.bitNumbers(bb).stream()
                .map(n -> new Move(fromRow, fromCol, n / 8, n % 8))
                .forEach(m -> moves.add(m));
    }

    private void verifyBitBoardToMoves(String[] bbString, List<Integer> solution) {
        List<Move> moves = new ArrayList<>();
        long bb = BitBoard.parseBitBoard(bbString);
        bitBoardToMoves(0, 3, bb, moves);

        assertThat(moves.size(), is(equalTo(solution.size() / 2)));

        List<Integer> tos = moves.stream()
                .map(m -> new Integer[]{m.getToRow(), m.getToCol()})
                .flatMap(Stream::of)
                .collect(Collectors.toList());

        assertThat(solution, is(equalTo(tos)));
    }

}
