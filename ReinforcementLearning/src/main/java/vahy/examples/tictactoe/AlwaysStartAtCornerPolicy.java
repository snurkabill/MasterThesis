package vahy.examples.tictactoe;

import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.RandomizedPolicy;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;

import java.util.SplittableRandom;

public class AlwaysStartAtCornerPolicy extends RandomizedPolicy<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> {

    private boolean isAnyCornerAlreadyPlayed = false;
    private final UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState> randomWalkPolicy;

    public AlwaysStartAtCornerPolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
        this.randomWalkPolicy = new UniformRandomWalkPolicy<>(random, policyId);
    }

    @Override
    public TicTacToeAction getDiscreteAction(StateWrapper<TicTacToeAction, DoubleVector, TicTacToeState> gameState) {
        if(!isAnyCornerAlreadyPlayed) {
            for (TicTacToeAction action : gameState.getAllPossibleActions()) {
                if(action == TicTacToeAction._0x0) {
                    isAnyCornerAlreadyPlayed = true;
                    return TicTacToeAction._0x0;
                } else if(action == TicTacToeAction._0x2) {
                    isAnyCornerAlreadyPlayed = true;
                    return TicTacToeAction._0x2;
                } else if(action == TicTacToeAction._2x0) {
                    isAnyCornerAlreadyPlayed = true;
                    return TicTacToeAction._2x0;
                } else if(action == TicTacToeAction._2x2) {
                    isAnyCornerAlreadyPlayed = true;
                    return TicTacToeAction._2x2;
                }
            }
        }
        return randomWalkPolicy.getDiscreteAction(gameState);
    }

    @Override
    public void updateStateOnPlayedAction(TicTacToeAction ticTacToeAction) {

    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TicTacToeAction, DoubleVector, TicTacToeState> gameState) {
        return null;
    }
}
