package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.impl.model.observation.DoubleVector;

public abstract class AbstractTrainableStateEvaluatingPolicySupplier<
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    private final TrainableRewardApproximator<TReward, TPlayerObservation> trainableRewardApproximator;

    public AbstractTrainableStateEvaluatingPolicySupplier(TrainableRewardApproximator<TReward, TPlayerObservation> trainableRewardApproximator) {
        this.trainableRewardApproximator = trainableRewardApproximator;
    }

    public TrainableRewardApproximator<TReward, TPlayerObservation> getTrainableRewardApproximator() {
        return trainableRewardApproximator;
    }
}
