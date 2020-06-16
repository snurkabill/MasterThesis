package vahy.examples.tictactoe;

import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.RandomizedPolicy;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;

import java.util.SplittableRandom;

public class AlwaysStartAtMiddlePolicy extends RandomizedPolicy<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> {

    private boolean isMiddleAlreadyPlayed = false;
    private final UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState> randomWalkPolicy;

    public AlwaysStartAtMiddlePolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
        this.randomWalkPolicy = new UniformRandomWalkPolicy<>(random, policyId);
    }

    @Override
    public TicTacToeAction getDiscreteAction(StateWrapper<TicTacToeAction, DoubleVector, TicTacToeState> gameState) {
        var allPossibleActions = gameState.getAllPossibleActions();
        if(!isMiddleAlreadyPlayed) {
            var isMiddleFree = false;
            for (TicTacToeAction action : allPossibleActions) {
                if(action == TicTacToeAction._1x1) {
                    isMiddleFree = true;
                    break;
                }
            }
            isMiddleAlreadyPlayed = true;
            if(isMiddleFree) {
                return TicTacToeAction._1x1;
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
