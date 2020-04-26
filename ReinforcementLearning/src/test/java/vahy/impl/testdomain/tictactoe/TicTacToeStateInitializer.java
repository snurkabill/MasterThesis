package vahy.impl.testdomain.tictactoe;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.SplittableRandom;

public class TicTacToeStateInitializer extends AbstractInitialStateSupplier<TicTacToeConfig, TicTacToeAction, DoubleVector, TicTacToeState, TicTacToeState> {

    public TicTacToeStateInitializer(TicTacToeConfig ticTacToeConfig, SplittableRandom random) {
        super(ticTacToeConfig, random);
    }

    @Override
    protected TicTacToeState createState_inner(TicTacToeConfig ticTacToeConfig, SplittableRandom random, PolicyMode policyMode) {
        return new TicTacToeState(
            new TicTacToeState.Symbol[][] {
                {
                    TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY,
                },
                {
                    TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY,
                },
                {
                    TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY, TicTacToeState.Symbol.EMPTY,
                }
            },
            random.nextBoolean(),
            9,
            Arrays.asList(TicTacToeAction.playerActions)
        );
    }
}
