package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchNodeFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends SearchNodeMetadata, TState extends State<TAction, TObservation, TState>> {

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> createNode(
        StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
        TAction action);
}
