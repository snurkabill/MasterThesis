package vahy.examples.tictactoe;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;

public class TicTacToeConfig extends ProblemConfig {

    private static final int dimension = 3;

    public TicTacToeConfig() {
        super(dimension * dimension,
            false,
            0,
            2,
            List.of(new PolicyCategoryInfo(true, 1, 2)),
            PolicyShuffleStrategy.CATEGORY_SHUFFLE);
    }

    public int getDimension() {
        return dimension;
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
