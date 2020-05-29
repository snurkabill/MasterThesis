package vahy.examples.tictactoe;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.ProblemConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TicTacToeConfig extends ProblemConfig {

    public TicTacToeConfig() {
        super(999, false, 0, 2, List.of(new PolicyCategoryInfo(true, 1, 2)), PolicyShuffleStrategy.CATEGORY_SHUFFLE);
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
