package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.observation.DoubleVector;

import java.util.LinkedHashMap;

public class AlphaGoNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends DoubleVector,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TObservation, AlphaGoNodeMetadata<TAction, TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public AlphaGoNodeMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public AlphaGoNodeMetadata<TAction, TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TObservation, AlphaGoNodeMetadata<TAction, TReward>, TState> parent,
                                                                          StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn,
                                                                          TAction appliedAction) {
        return new AlphaGoNodeMetadata<>(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward(),
            parent != null ? parent.getSearchNodeMetadata().getChildPriorProbabilities().get(appliedAction) : 0.0d,
            new LinkedHashMap<>()
        );
    }
}