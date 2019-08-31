package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;

public class MonteCarloTreeSearchMetadataFactory<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> {

    private final RewardAggregator rewardAggregator;

    public MonteCarloTreeSearchMetadataFactory(RewardAggregator rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
    }

    @Override
    public MonteCarloTreeSearchMetadata createSearchNodeMetadata(SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> parent,
                                                                          StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                                          TAction appliedAction) {
        return new MonteCarloTreeSearchMetadata(
            parent != null ? rewardAggregator.aggregate(parent.getSearchNodeMetadata().getCumulativeReward(), stateRewardReturn.getReward()) : stateRewardReturn.getReward(),
            stateRewardReturn.getReward(),
            rewardAggregator.emptyReward());
    }

    @Override
    public MonteCarloTreeSearchMetadata createEmptyNodeMetadata() {
        return new MonteCarloTreeSearchMetadata(rewardAggregator.emptyReward(), rewardAggregator.emptyReward(), rewardAggregator.emptyReward());
    }
}
