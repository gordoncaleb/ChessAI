package com.gordoncaleb.chess.unit.engine.mocks;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.engine.score.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MockScorer extends StaticScorer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticScorer.class);

    private Random random;
    private List<Integer> scores;

    public MockScorer(long seed, int maxDepth) {
        random = new Random(seed);
        scores = new ArrayList<>();
        buildScores(maxDepth);
    }

    private void buildScores(int maxDepth) {
        final int scoreSize = (int) Math.pow(2, maxDepth + 1) - 1;
        Set<Integer> scoreSet = new HashSet<>();
        while (scoreSet.size() < scoreSize) {
            scoreSet.add(random.nextInt(2 * Values.CHECKMATE_MOVE) - Values.CHECKMATE_MOVE);
        }

        scores.addAll(scoreSet);
    }

    @Override
    public int staticScore(Board b) {
        List<Move> moves = b.getMoveHistory();

        int left = 0;
        int right = scores.size() - 1;
        int mid = (left + right) / 2;

        for (Move m : moves) {
            if (m.equals(MockBoard.LeftMove)) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }

            mid = (left + right) / 2;
        }

        final int score = scores.get(mid);
        return score;
    }

}
