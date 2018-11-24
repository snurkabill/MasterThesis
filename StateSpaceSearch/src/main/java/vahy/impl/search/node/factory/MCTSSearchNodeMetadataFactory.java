package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;

public class MCTSSearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public MCTSSearchNodeMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public MCTSNodeMetadata<TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> parent,
                                                              StateRewardReturn<TAction, TReward, TObservation, TState>stateRewardReturn) {
        return new MCTSNodeMetadata<>(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward());
    }
}
