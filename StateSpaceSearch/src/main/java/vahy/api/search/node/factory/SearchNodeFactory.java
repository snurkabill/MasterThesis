package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchNodeFactory<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createNode(
        StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn,
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction action);
}
