package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;

import java.util.LinkedHashMap;

public class AlphaGoNodeMetadataFactory<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends DoubleVector,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction>, TState> {

    @Override
    public AlphaGoNodeMetadata<TAction> createEmptyNodeMetadata() {
        return new AlphaGoNodeMetadata<TAction>(DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward(), 0.0d, new LinkedHashMap<>());
    }

    @Override
    public AlphaGoNodeMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction>, TState> parent,
                                                                 StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn,
                                                                 TAction appliedAction) {
        return new AlphaGoNodeMetadata<TAction>(
            parent != null ? DoubleScalarRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            DoubleScalarRewardAggregator.emptyReward(),
            parent != null ? parent.getSearchNodeMetadata().getChildPriorProbabilities().get(appliedAction) : 0.0d,
            new LinkedHashMap<>()
        );

    }
}
