package vahy.impl.search.tree;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeExpander.NodeExpander;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.SearchTree;
import vahy.api.search.update.TreeUpdater;

public class SearchTreeImpl<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements SearchTree<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root;
    private final NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeExpander;
    private final TreeUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> treeUpdater;

    protected SearchTreeImpl(
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root,
        NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeSelector,
        NodeExpander<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeExpander,
        TreeUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> treeUpdater) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.nodeSelector.addNode(root);
    }

    @Override
    public void updateTree() {
        // if there is something to update
        SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectedNodeForExpansion = nodeSelector.selectNextNode();
        nodeExpander.expandNode(selectedNodeForExpansion);
        nodeSelector.addNodes(selectedNodeForExpansion.getChildNodeMap().values());
        treeUpdater.updateTree(selectedNodeForExpansion);
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> getRoot() {
        return root;
    }

}
