package vahy.api.model;

public interface StateRewardReturn<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation>> {

    TReward getReward();

    TState getState();
}
