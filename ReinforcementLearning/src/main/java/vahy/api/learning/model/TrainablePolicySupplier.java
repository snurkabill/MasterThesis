package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.PolicySupplier;

public interface TrainablePolicySupplier<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation>
    extends PolicySupplier<TAction, TReward, TObservation> {

    SupervisedTrainableModel getTrainableStateEvaluator();

}
