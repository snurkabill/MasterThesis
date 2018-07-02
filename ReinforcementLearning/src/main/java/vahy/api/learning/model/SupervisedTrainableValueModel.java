package vahy.api.learning.model;

import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface SupervisedTrainableValueModel<TReward extends Reward, TObservation extends Observation> {

    TReward approximateReward(TObservation observationAggregation);

    void fit(List<TObservation> inputList, List<TReward> rewardList);

}
