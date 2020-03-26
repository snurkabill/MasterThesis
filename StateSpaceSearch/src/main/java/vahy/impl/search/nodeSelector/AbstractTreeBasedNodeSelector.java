package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

public abstract class AbstractTreeBasedNodeSelector<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root;

    @Override
    public void setNewRoot(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root) {
        this.root = root;
    }

    protected void checkRoot() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
    }

    public abstract SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> selectNextNode();

}
