package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.Predictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class ValuePolicy<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>>
    extends RandomizedPolicy<TAction, DoubleVector, TState, PolicyRecordBase> {

    private final Predictor<DoubleVector> valuePredictor;
    private final double explorationConstant;

    public ValuePolicy(SplittableRandom random, int policyId, Predictor<DoubleVector> valuePredictor, double explorationConstant) {
        super(random, policyId);
        this.valuePredictor = valuePredictor;
        this.explorationConstant = explorationConstant;
    }

    private ImmutableTuple<Double, TAction> getMaxActionValuePair(StateWrapper<TAction, DoubleVector, TState> gameState) {
        if(!gameState.isPlayerTurn()) {
            throw new IllegalStateException("Policy [" + policyId + "] is not on turn.");
        }
        TAction[] actions = gameState.getAllPossibleActions();
        var rewards = new double[actions.length];
        var observations = new DoubleVector[actions.length];

        for (int i = 0; i < actions.length; i++) {
            var applied = gameState.applyAction(actions[i]);
            rewards[i] = applied.getReward();
            observations[i] = applied.getState().getObservation();
        }
        var predictions = valuePredictor.apply(observations);


        double max = rewards[0] + predictions[0][0];
        TAction maxAction = actions[0];
        for (int i = 1; i < actions.length; i++) {
            var value = rewards[i] + predictions[i][0];
            if(value > max) {
                max = value;
                maxAction = actions[i];
            }
        }
        return new ImmutableTuple<>(max, maxAction);
    }

    @Override
    public int getPolicyId() {
        return policyId;
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, DoubleVector, TState> gameState) {
        return new double[0];
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, DoubleVector, TState> gameState) {
        if(explorationConstant == 0.0) {
            return getMaxActionValuePair(gameState).getSecond();
        } else if(random.nextDouble() < explorationConstant) {
            TAction[] actions = gameState.getAllPossibleActions();
            return actions[random.nextInt(actions.length)];
        } else {
            return getMaxActionValuePair(gameState).getSecond();
        }
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        // this is it
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, DoubleVector, TState> gameState) {
        return new PolicyRecordBase(new double[0], 0.0);
    }
}
