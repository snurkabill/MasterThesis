package vahy.api.search.nodeEvaluator;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.NodeMetadata;

public interface NodeEvaluator<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>> {

    int evaluateNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode);
}
