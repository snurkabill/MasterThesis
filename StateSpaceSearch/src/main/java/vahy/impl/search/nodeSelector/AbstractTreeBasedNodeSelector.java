package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

public abstract class AbstractTreeBasedNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root;

    @Override
    public void setNewRoot(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        this.root = root;
    }

    protected void checkRoot() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
    }

    public abstract SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode();

}
