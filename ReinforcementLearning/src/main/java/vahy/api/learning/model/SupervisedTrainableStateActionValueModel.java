package vahy.api.learning.model;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.reward.Reward;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface SupervisedTrainableStateActionValueModel<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    void fit(List<ImmutableTuple<List<TObservation>, TAction>> inputList, List<TReward> rewardList);

}
