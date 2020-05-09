package vahy.paperGenerics.metadata;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperGenerics.PaperState;

import java.util.EnumMap;

public class PaperMetadataFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends PaperState<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, PaperMetadata<TAction>, TState> {

    private final Class<TAction> actionClass;

    public PaperMetadataFactory(Class<TAction> actionClass) {
        this.actionClass = actionClass;
    }

    @Override
    public PaperMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> parent,
                                                           StateRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                           TAction appliedAction) {
        double reward = stateRewardReturn.getReward();
        TState state = stateRewardReturn.getState();
        if(parent != null) {
            var searchNodeMetadata = parent.getSearchNodeMetadata();
            return new PaperMetadata<>(
                DoubleScalarRewardAggregator.aggregate(searchNodeMetadata.getCumulativeReward(), reward),
                reward,
                DoubleScalarRewardAggregator.emptyReward(),
                searchNodeMetadata.getChildPriorProbabilities().get(appliedAction),
                state.isFinalState() ? (state.isRiskHit() ? 1.0 : 0.0) : Double.NaN,
                new EnumMap<>(actionClass)
            );
        } else {
            return new PaperMetadata<>(
                reward,
                reward,
                DoubleScalarRewardAggregator.emptyReward(),
                Double.NaN,
                state.isFinalState() ? (state.isRiskHit() ? 1.0 : 0.0) : Double.NaN,
                new EnumMap<>(actionClass)
            );
        }
    }

    @Override
    public PaperMetadata<TAction> createEmptyNodeMetadata() {
        return new PaperMetadata<>(
            DoubleScalarRewardAggregator.emptyReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            0.0d,
            0.0,
            new EnumMap<>(actionClass)
        );
    }
}
