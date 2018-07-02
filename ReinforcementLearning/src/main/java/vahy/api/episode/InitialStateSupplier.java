package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;

public interface InitialStateSupplier<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    State<TAction, TReward, TObservation> createInitialState();
}
