package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.policy.ExploringPolicy;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.Predictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public class ValuePolicy<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> extends ExploringPolicy<TAction, DoubleVector, TState, PolicyRecordBase> {

    private final Predictor<DoubleVector> valuePredictor;

    public ValuePolicy(SplittableRandom random, int policyId, Predictor<DoubleVector> valuePredictor, double explorationConstant) {
        super(random, policyId, explorationConstant);
        this.valuePredictor = valuePredictor;
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
    public void updateStateOnPlayedAction(TAction action) {
        // this is it
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, DoubleVector, TState> gameState) {
        return new PolicyRecordBase(this.playingDistribution.getDistribution(), this.playingDistribution.getPredictedReward());
    }

    @Override
    protected PlayingDistribution<TAction> inferenceBranch(StateWrapper<TAction, DoubleVector, TState> gameState) {
        var actionValuePair = getMaxActionValuePair(gameState);
        return new PlayingDistribution<>(actionValuePair.getSecond(), actionValuePair.getFirst(), EMPTY_ARRAY);
    }

    @Override
    protected PlayingDistribution<TAction> explorationBranch(StateWrapper<TAction, DoubleVector, TState> gameState) {
        TAction[] actions = gameState.getAllPossibleActions();
        var actionIndex = random.nextInt(actions.length);
        var action = actions[actionIndex];
        var applied = gameState.applyAction(action);
        var value = applied.getReward() + valuePredictor.apply(applied.getState().getObservation())[0];
        return new PlayingDistribution<>(action, value, EMPTY_ARRAY);
    }
}
