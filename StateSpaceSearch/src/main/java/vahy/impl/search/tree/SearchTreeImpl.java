package vahy.impl.search.tree;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeExpander.NodeExpander;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.treeUpdater.TreeUpdater;

public class SearchTreeImpl<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TReward, TObservation>>
        implements SearchTree<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> root;
    private final NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeExpander<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeExpander;
    private final TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater;

    protected SearchTreeImpl(
            SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> root,
            NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector,
            NodeExpander<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeExpander,
            TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.nodeSelector.addNode(root);
    }

    @Override
    public void updateTree() {

        // if there is something to update

        SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> selectedNodeForExpansion = nodeSelector.selectNextNode();
        nodeExpander.expandNode(selectedNodeForExpansion);
        treeUpdater.updateTree(selectedNodeForExpansion);
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TState, TSearchNodeMetadata> getRoot() {
        return root;
    }

}
