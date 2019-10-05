package vahy.paperGenerics.metadata;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperGenerics.PaperState;

import java.util.LinkedHashMap;

public class PaperMetadataFactory<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    @Override
    public PaperMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> parent,
                                                                          StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn,
                                                                          TAction appliedAction) {
        return new PaperMetadata<>(
            parent != null ? DoubleScalarRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            parent != null && !parent.getSearchNodeMetadata().getChildPriorProbabilities().isEmpty() ? parent.getSearchNodeMetadata().getChildPriorProbabilities().get(appliedAction) : Double.NaN,
            stateRewardReturn.getState().isFinalState() ? (stateRewardReturn.getState().isRiskHit() ? 1.0 : 0.0) : Double.NaN,
            new LinkedHashMap<>()
        );
    }

    @Override
    public PaperMetadata<TAction> createEmptyNodeMetadata() {
        return new PaperMetadata<>(
            DoubleScalarRewardAggregator.emptyReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            0.0d,
            0.0,
            new LinkedHashMap<>()
        );
    }
}
