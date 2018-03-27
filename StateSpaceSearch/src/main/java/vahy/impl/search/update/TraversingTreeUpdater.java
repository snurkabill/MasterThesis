package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.Reward;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.api.search.update.TreeUpdater;

public class TraversingTreeUpdater<
        TAction extends Action,
        TReward extends Reward,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward>,
        TState extends State<TAction, TReward, TObservation>>
        implements TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final NodeTransitionUpdater<TAction, TReward, TSearchNodeMetadata> nodeTransitionUpdater;

    public TraversingTreeUpdater(NodeTransitionUpdater<TAction, TReward, TSearchNodeMetadata> nodeTransitionUpdater) {
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public void updateTree(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> expandedNode) {
        while(!expandedNode.isRoot()) {
            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> parent = expandedNode.getParent();
            nodeTransitionUpdater.applyUpdate(parent.getSearchNodeMetadata(), expandedNode.getSearchNodeMetadata());
            expandedNode = parent;
        }
    }
}
