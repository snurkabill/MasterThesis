package vahy.api.learning.model;

import vahy.api.model.observation.Observation;

import java.util.List;

public interface SupervisedTrainableValueModel<TObservation extends Observation> {

    double approximateReward(TObservation observationAggregation);

    void fit(List<TObservation> inputList, List<Double> rewardList);

}
