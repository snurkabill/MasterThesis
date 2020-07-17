package vahy.api.search.node.factory;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;

public interface SearchNodeMetadataFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>> {

    int getInGameEntityCount();

    TSearchNodeMetadata createSearchNodeMetadata(SearchNode<TAction,  TObservation, TSearchNodeMetadata, TState> parent, StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn, TAction appliedAction);

    TSearchNodeMetadata createEmptyNodeMetadata();

}
