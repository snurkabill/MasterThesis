package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;

public class MonteCarloTreeSearchMetadataFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, MonteCarloTreeMetadata, TState> {


    @Override
    public MonteCarloTreeMetadata createSearchNodeMetadata(SearchNode<TAction, TObservation, MonteCarloTreeMetadata, TState> parent,
                                                           StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                           TAction appliedAction) {
        var allPlayerRewards = stateRewardReturn.getAllPlayerRewards();
        if(parent == null) {
            return new MonteCarloTreeMetadata(allPlayerRewards, allPlayerRewards);
        } else {
            return new MonteCarloTreeMetadata(
                DoubleVectorRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getAllPlayerRewards()),
                stateRewardReturn.getAllPlayerRewards());
        }
    }

    @Override
    public MonteCarloTreeMetadata createEmptyNodeMetadata(int entityInGameCount) {
        return new MonteCarloTreeMetadata(DoubleVectorRewardAggregator.emptyReward(entityInGameCount), DoubleVectorRewardAggregator.emptyReward(entityInGameCount));
    }
}
