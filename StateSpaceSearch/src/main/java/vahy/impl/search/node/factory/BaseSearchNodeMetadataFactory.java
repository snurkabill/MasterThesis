package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;

public class BaseSearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, BaseSearchNodeMetadata<TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public BaseSearchNodeMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public BaseSearchNodeMetadata<TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, BaseSearchNodeMetadata<TReward>, TState> parent,
                                                                    StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                                    TAction appliedAction) {
        return new BaseSearchNodeMetadata<>(
            rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward());
    }

    @Override
    public BaseSearchNodeMetadata<TReward> createEmptyNodeMetadata() {
        return new BaseSearchNodeMetadata<>(rewardAggregator.emptyReward(), rewardAggregator.emptyReward(), rewardAggregator.emptyReward());
    }
}
