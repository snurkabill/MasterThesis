package vahy.impl.search.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;

public class MCTSMetadataFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, MCTSMetadata, TState> {

    private final int inGameEntityCount;

    public MCTSMetadataFactory(int inGameEntityCount) {
        this.inGameEntityCount = inGameEntityCount;
    }

    @Override
    public int getInGameEntityCount() {
        return inGameEntityCount;
    }

    @Override
    public MCTSMetadata createSearchNodeMetadata(SearchNode<TAction, TObservation, MCTSMetadata, TState> parent, StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn, TAction appliedAction) {
        var allPlayerRewards = stateRewardReturn.getAllPlayerRewards();
        if(parent == null) {
            return new MCTSMetadata(allPlayerRewards, allPlayerRewards);
        } else {
            return new MCTSMetadata(DoubleVectorRewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), allPlayerRewards), allPlayerRewards);
        }
    }

    @Override
    public MCTSMetadata createEmptyNodeMetadata() {
        return new MCTSMetadata(DoubleVectorRewardAggregator.emptyReward(inGameEntityCount), DoubleVectorRewardAggregator.emptyReward(inGameEntityCount));
    }
}
