package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

public abstract class AbstractTreeBasedNodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root;

    protected abstract TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node);

    @Override
    public void setNewRoot(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root) {
        this.root = root;
    }

    protected void checkRoot() {
        if(root == null) {
            throw new IllegalStateException("Root was not initialized");
        }
    }

    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> selectNextNode() {
        checkRoot();
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node = root;
        while(!node.isLeaf()) {
            TAction bestAction = getBestAction(node);
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }
}
