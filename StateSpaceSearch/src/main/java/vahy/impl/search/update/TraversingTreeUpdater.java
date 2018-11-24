package vahy.impl.search.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.api.search.update.TreeUpdater;

public class TraversingTreeUpdater<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(TraversingTreeUpdater.class);
    private final NodeTransitionUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeTransitionUpdater;

    public TraversingTreeUpdater(NodeTransitionUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeTransitionUpdater) {
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public void updateTree(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> expandedNode) {
        if(expandedNode.isRoot()) {
            return;
        }
        SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> traversingNode = expandedNode;
        int i = 0;
        do {
            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> traversingNodeParent = traversingNode.getParent();
            nodeTransitionUpdater.applyUpdate(expandedNode, traversingNodeParent, traversingNode);
            traversingNode = traversingNodeParent;
            i++;
        } while(!traversingNode.isRoot());

        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }
}
