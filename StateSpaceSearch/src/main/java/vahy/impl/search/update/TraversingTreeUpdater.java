package vahy.impl.search.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
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

    private static final Logger logger = LoggerFactory.getLogger(TraversingTreeUpdater.class);
    private final NodeTransitionUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeTransitionUpdater;

    public TraversingTreeUpdater(NodeTransitionUpdater<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> nodeTransitionUpdater) {
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public void updateTree(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
        int i = 0;
        while (!expandedNode.isRoot()) {
            SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> parent = expandedNode.getParent();
            TReward previousEstimate = parent.getSearchNodeMetadata().getEstimatedTotalReward();
            nodeTransitionUpdater.applyUpdate(parent, expandedNode, expandedNode.getAppliedParentAction());
            if(parent.getSearchNodeMetadata().getEstimatedTotalReward().compareTo(previousEstimate) == 0) {
                break;
            }
            expandedNode = parent;
            i++;
        }
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }
}
