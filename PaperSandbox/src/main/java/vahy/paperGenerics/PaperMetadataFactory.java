package vahy.paperGenerics;

import vahy.api.model.Action;
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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> {

    private final RewardAggregator<TReward> rewardAggregator;

    public PaperMetadataFactory(RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public PaperMetadata<TAction, TReward> createSearchNodeMetadata(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, TReward>, TState> parent,
                                                                          StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn,
                                                                          TAction appliedAction) {
        return new PaperMetadata<>(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward(),
            parent != null && !parent.getSearchNodeMetadata().getChildPriorProbabilities().isEmpty() ? parent.getSearchNodeMetadata().getChildPriorProbabilities().get(appliedAction) : Double.NaN,
            stateRewardReturn.getState().isFinalState() ? (stateRewardReturn.getState().isRiskHit() ? 1.0 : 0.0) : Double.NaN,
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
