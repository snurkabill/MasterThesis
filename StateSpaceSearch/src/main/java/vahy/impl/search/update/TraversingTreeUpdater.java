package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.api.search.update.TreeUpdater;

public class TraversingTreeUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements TreeUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final NodeTransitionUpdater<TAction, TReward, TStateActionMetadata, TSearchNodeMetadata> nodeTransitionUpdater;

    public TraversingTreeUpdater(NodeTransitionUpdater<TAction, TReward, TStateActionMetadata, TSearchNodeMetadata> nodeTransitionUpdater) {
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public void updateTree(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
        while (!expandedNode.isRoot()) {
            SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent = expandedNode.getParent();
            nodeTransitionUpdater.applyUpdate(parent.getSearchNodeMetadata(), expandedNode.getSearchNodeMetadata(), expandedNode.getAppliedParentAction());
            expandedNode = parent;
        }
    }
}
