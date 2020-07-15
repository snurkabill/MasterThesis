package vahy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.NodeSelector;

public class SelectorTestHelper<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>>
    implements NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {


    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        return null;
    }

}
