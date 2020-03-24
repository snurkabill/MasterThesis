package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchNodeMetadataFactory<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    TSearchNodeMetadata createSearchNodeMetadata(SearchNode<TAction,  TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
                                                 StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>stateRewardReturn,
                                                 TAction appliedAction);

    TSearchNodeMetadata createEmptyNodeMetadata();

}
