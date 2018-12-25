package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;

public class MonteCarloTreeSearchMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata<TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public MonteCarloTreeSearchMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public MonteCarloTreeSearchMetadata<TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata<TReward>, TState> parent,
                                                                          StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                                          TAction appliedAction) {
        return new MonteCarloTreeSearchMetadata<>(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward());
    }

    @Override
    public MonteCarloTreeSearchMetadata<TReward> createEmptyNodeMetadata() {
        return new MonteCarloTreeSearchMetadata<>(rewardAggregator.emptyReward(), rewardAggregator.emptyReward(), rewardAggregator.emptyReward());
    }
}
