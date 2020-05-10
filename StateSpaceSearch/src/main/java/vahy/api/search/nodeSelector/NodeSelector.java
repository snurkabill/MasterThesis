package vahy.api.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;

public interface NodeSelector<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends SearchNodeMetadata, TState extends State<TAction, TObservation, TState>> {

    void setNewRoot(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root);

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode();

}
