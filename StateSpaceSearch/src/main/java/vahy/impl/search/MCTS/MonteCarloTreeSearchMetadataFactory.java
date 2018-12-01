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
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, MonteCarloTreeSearchMetadata<TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public MonteCarloTreeSearchMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public MonteCarloTreeSearchMetadata<TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TObservation, MonteCarloTreeSearchMetadata<TReward>, TState> parent,
                                                                          StateRewardReturn<TAction, TReward, TObservation, TState>stateRewardReturn,
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
