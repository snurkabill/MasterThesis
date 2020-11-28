package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;

import java.util.Map;

public interface SearchNodeFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>> {

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> createNode(StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                                              SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
                                                                              TAction action);

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> createNode(StateWrapper<TAction, TObservation, TState> initialState,
                                                                              TSearchNodeMetadata metadata,
                                                                              Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap);

    SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> getSearchNodeMetadataFactory();

}
