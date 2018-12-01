package vahy.paperGenerics;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleReward;

import java.util.LinkedHashMap;

public class PaperMetadataFactory<
    TAction extends Action,
    TReward extends DoubleReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>> implements SearchNodeMetadataFactory<TAction, TReward, TObservation, PaperMetadata<TAction, TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public PaperMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public PaperMetadata<TAction, TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TObservation, PaperMetadata<TAction, TReward>, TState> parent,
                                                                          StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn,
                                                                          TAction appliedAction) {
        return new PaperMetadata<>(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward(),
            parent != null ? parent.getSearchNodeMetadata().getChildPriorProbabilities().get(appliedAction) : Double.NaN,
            Double.NaN,
            new LinkedHashMap<>()
        );
    }

    @Override
    public PaperMetadata<TAction, TReward> createEmptyNodeMetadata() {
        return new PaperMetadata<>(
            rewardAggregator.emptyReward(),
            rewardAggregator.emptyReward(),
            rewardAggregator.emptyReward(),
            0.0d,
            0.0,
            new LinkedHashMap<>()
        );
    }
}
