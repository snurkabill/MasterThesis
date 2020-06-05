package vahy.impl.search.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.api.search.update.TreeUpdater;

public class TraversingTreeUpdater<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(TraversingTreeUpdater.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    private final NodeTransitionUpdater<TAction, TObservation, TSearchNodeMetadata, TState> nodeTransitionUpdater;

    public TraversingTreeUpdater(NodeTransitionUpdater<TAction, TObservation, TSearchNodeMetadata, TState> nodeTransitionUpdater) {
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public void updateTree(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> expandedNode) {
        if(expandedNode.isRoot()) {
            return;
        }
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> traversingNode = expandedNode;
        int i = 0;
        do {
            SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> traversingNodeParent = traversingNode.getParent();
            nodeTransitionUpdater.applyUpdate(expandedNode, traversingNodeParent, traversingNode);
            traversingNode = traversingNodeParent;
            i++;
        } while(!traversingNode.isRoot());
        if(TRACE_ENABLED) {
            logger.trace("Traversing updated traversed [{}] tree levels", i);
        }
    }
}
