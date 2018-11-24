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
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>> {

    TSearchNodeMetadata createSearchNodeMetadata(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent,
                                                 StateRewardReturn<TAction, TReward, TObservation, TState>stateRewardReturn,
                                                 TAction appliedAction);

}
