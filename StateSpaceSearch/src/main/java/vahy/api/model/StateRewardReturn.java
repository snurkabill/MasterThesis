package vahy.api.model;

public interface StateRewardReturn<TReward extends Reward, TState extends State<? extends Action, TReward, ? extends Observation>> {

    TReward getReward();

    TState getState();
}
