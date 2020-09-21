package vahy.integration.tictactoe;

import vahy.examples.tictactoe.AlwaysStartAtCornerPolicy;
import vahy.examples.tictactoe.AlwaysStartAtMiddlePolicy;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.runner.PolicyDefinition;

import java.util.ArrayList;

public abstract class AbstractTicTacToeConvergenceTest {

    protected AbstractTicTacToeConvergenceTest() {};

    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createUniformPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId),
            new ArrayList<>(0)
        );
    }

    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtMiddlePolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtMiddlePolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtCornerPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtCornerPolicy(random, policyId),
            new ArrayList<>(0)
        );
    }
}
