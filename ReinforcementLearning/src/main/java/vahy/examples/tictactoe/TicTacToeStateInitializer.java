package vahy.examples.tictactoe;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.SplittableRandom;

public class TicTacToeStateInitializer extends AbstractInitialStateSupplier<TicTacToeConfig, TicTacToeAction, DoubleVector, TicTacToeState> {

    public TicTacToeStateInitializer(TicTacToeConfig ticTacToeConfig, SplittableRandom random) {
        super(ticTacToeConfig, random);
    }

    @Override
    protected TicTacToeState createState_inner(TicTacToeConfig ticTacToeConfig, SplittableRandom random, PolicyMode policyMode) {

        var ticTacToeArray = new TicTacToeState.Symbol[ticTacToeConfig.getDimension()][];
        for (int i = 0; i < ticTacToeArray.length; i++) {
            ticTacToeArray[i] = new TicTacToeState.Symbol[ticTacToeConfig.getDimension()];
            Arrays.fill(ticTacToeArray[i], TicTacToeState.Symbol.EMPTY);
        }
        return new TicTacToeState(
            ticTacToeConfig.getDimension(),
            ticTacToeArray,
            true,
            ticTacToeConfig.getDimension() * ticTacToeConfig.getDimension(),
            Arrays.asList(TicTacToeAction.values())
        );
    }
}
