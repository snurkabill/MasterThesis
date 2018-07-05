package vahy.api.learning.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.function.Function;

public interface TrainableRewardApproximator<TReward extends Reward, TObservation extends Observation> extends Function<TObservation, TReward> {

    void train(List<ImmutableTuple<TObservation, TReward>> episodeData);

}
