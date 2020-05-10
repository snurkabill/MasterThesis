package vahy.api.search.tree;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface SearchTree<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends SearchNodeMetadata, TState extends State<TAction, TObservation, TState>> {

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> getRoot();

    StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction action);

    boolean updateTree();

}
