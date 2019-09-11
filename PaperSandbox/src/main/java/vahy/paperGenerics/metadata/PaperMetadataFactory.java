package vahy.paperGenerics.metadata;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.paperGenerics.PaperState;

import java.util.LinkedHashMap;

public class PaperMetadataFactory<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    private final RewardAggregator rewardAggregator;

    public PaperMetadataFactory(RewardAggregator rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public PaperMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> parent,
                                                                          StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn,
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
    public PaperMetadata<TAction> createEmptyNodeMetadata() {
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
