package vahy.api.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.NodeMetadata;

public interface NodeSelector<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>> {

    SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root);

}
