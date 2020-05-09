package vahy.impl.testdomain.tictactoe;

import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedPolicy;
import vahy.impl.policy.UniformRandomWalkPolicy;

import java.util.List;
import java.util.SplittableRandom;

public class AlwaysStartAtMiddlePolicy extends RandomizedPolicy<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> {

    private boolean isMiddleAlreadyPlayed = false;
    private UniformRandomWalkPolicy<TicTacToeAction, DoubleVector, TicTacToeState> randomWalkPolicy;

    public AlwaysStartAtMiddlePolicy(SplittableRandom random, int policyId) {
        super(random, policyId);
        this.randomWalkPolicy = new UniformRandomWalkPolicy<>(random, policyId);
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TicTacToeAction, DoubleVector, TicTacToeState> gameState) {
        return new double[0];
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
    public void updateStateOnPlayedActions(List<TicTacToeAction> opponentActionList) {

    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TicTacToeAction, DoubleVector, TicTacToeState> gameState) {
        return null;
    }
}
