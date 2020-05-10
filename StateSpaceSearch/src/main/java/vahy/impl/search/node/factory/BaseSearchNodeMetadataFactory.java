package vahy.impl.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;

public class BaseSearchNodeMetadataFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, BaseSearchNodeMetadata, TState> {

    @Override
    public BaseSearchNodeMetadata createSearchNodeMetadata(SearchNode<TAction, TObservation, BaseSearchNodeMetadata, TState> parent,
                                                           StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                           TAction appliedAction)
    {
        return new BaseSearchNodeMetadata(
            DoubleScalarRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()),
            stateRewardReturn.getReward(),
            DoubleScalarRewardAggregator.emptyReward());
    }

    @Override
    public BaseSearchNodeMetadata createEmptyNodeMetadata() {
        return new BaseSearchNodeMetadata(DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward());
    }
}
