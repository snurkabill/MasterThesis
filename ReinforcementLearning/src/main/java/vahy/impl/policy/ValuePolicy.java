package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.Predictor;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class ValuePolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>>
    extends RandomizedPolicy<TAction, TObservation, TState, PolicyRecordBase> {

    private final Predictor<TObservation> valuePredictor;
    private final double explorationConstant;

    public ValuePolicy(SplittableRandom random, int policyId, Predictor<TObservation> valuePredictor, double explorationConstant) {
        super(random, policyId);
        this.valuePredictor = valuePredictor;
        this.explorationConstant = explorationConstant;
    }

    private ImmutableTuple<Double, TAction> getMaxActionValuePair(StateWrapper<TAction, TObservation, TState> gameState) {
        if(!gameState.isPlayerTurn()) {
            throw new IllegalStateException("Policy [" + policyId + "] is not on turn.");
        }
        TAction[] actions = gameState.getAllPossibleActions();
        TAction maxAction = actions[0];
        var applied = gameState.applyAction(maxAction);
        double max = applied.getReward() + valuePredictor.apply(applied.getState().getObservation())[0];
        for (int i = 1; i < actions.length; i++) {
            applied = gameState.applyAction(actions[i]);
            var value = applied.getReward() + valuePredictor.apply(applied.getState().getObservation())[0];
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
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        return new double[0];
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
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
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        return new PolicyRecordBase(getActionProbabilityDistribution(gameState), getMaxActionValuePair(gameState).getFirst());
    }
}
