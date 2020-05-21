package vahy.examples.tictactoe;

import vahy.api.experiment.ProblemConfig;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TicTacToeConfig extends ProblemConfig {

    public TicTacToeConfig() {
        super(999, false, 0, 2, Stream.of(0, 1).collect(Collectors.toSet()));
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
