package vahy.api.learning.model;

import vahy.api.approx.StateValueApproximator;
import vahy.api.model.Observation;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface SupervisedTrainableStateValueModel<TReward extends Reward, TObservation extends Observation> extends StateValueApproximator<TReward, TObservation> {

    void fit(List<List<TObservation>> inputMatrix, List<TReward> rewardList);

}
