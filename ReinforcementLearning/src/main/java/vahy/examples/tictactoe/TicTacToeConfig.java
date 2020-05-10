package vahy.examples.tictactoe;

import vahy.api.experiment.ProblemConfig;

public class TicTacToeConfig extends ProblemConfig {

    public TicTacToeConfig() {
        super(999, false);
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
