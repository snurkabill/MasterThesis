package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;

public class MonteCarloTreeSearchMetadataFactory<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> {


    @Override
    public MonteCarloTreeSearchMetadata createSearchNodeMetadata(SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> parent,
                                                                          StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                                          TAction appliedAction) {
        return new MonteCarloTreeSearchMetadata(
            parent != null ? DoubleScalarRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            DoubleScalarRewardAggregator.emptyReward());
    }

    @Override
    public MonteCarloTreeSearchMetadata createEmptyNodeMetadata() {
        return new MonteCarloTreeSearchMetadata(DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward(), DoubleScalarRewardAggregator.emptyReward());
    }
}
