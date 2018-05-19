package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface Episode<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> runEpisode();
}
