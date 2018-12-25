package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchNodeMetadataFactory<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    TSearchNodeMetadata createSearchNodeMetadata(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                                                 StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                 TAction appliedAction);

    TSearchNodeMetadata createEmptyNodeMetadata();

}
