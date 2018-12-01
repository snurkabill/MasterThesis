package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface Episode<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, TReward, TObservation, TState>> {

    void runEpisode();

    boolean isEpisodeAlreadySimulated();

    List<StateRewardReturn<TAction, TReward, TObservation, TState>> getEpisodeStateRewardReturnList();

    List<StateActionReward<TAction, TReward, TObservation, TState>> getEpisodeStateActionRewardList();

    TState getFinalState();

}
