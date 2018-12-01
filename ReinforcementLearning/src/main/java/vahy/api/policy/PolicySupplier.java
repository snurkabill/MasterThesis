package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;

public interface PolicySupplier<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, TReward, TObservation, TState>> {

    Policy<TAction, TReward, TObservation, TState> initializePolicy(TState initialState);

}
