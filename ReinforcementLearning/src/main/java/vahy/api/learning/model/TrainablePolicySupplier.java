package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.policy.PolicySupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainablePolicySupplier<TAction extends Action, TReward extends DoubleVectorialReward, TObservation extends DoubleVectorialObservation>
    extends PolicySupplier<TAction, TReward, TObservation> {

    void train(List<ImmutableTuple<TObservation, TReward>> episodeData);
}
