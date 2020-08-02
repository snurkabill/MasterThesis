package vahy.examples.tictactoe;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;

public class TicTacToeConfig extends ProblemConfig {

    private static final int DIMENSION = 3;

    public TicTacToeConfig() {
        super(DIMENSION * DIMENSION,
            false,
            0,
            2,
            List.of(new PolicyCategoryInfo(true, 1, 2)),
            PolicyShuffleStrategy.CATEGORY_SHUFFLE);
    }

    public int getDimension() {
        return DIMENSION;
    }

    @Override
    public String toLog() {
        return null;
    }

    @Override
    public String toFile() {
        return null;
    }
}
