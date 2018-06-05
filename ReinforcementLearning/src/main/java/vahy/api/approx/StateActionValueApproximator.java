package vahy.api.approx;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface StateActionValueApproximator<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    TReward approximate(List<TObservation> observationList, TAction action); // list is here only if we would like to approximate reward based on observation history
}
