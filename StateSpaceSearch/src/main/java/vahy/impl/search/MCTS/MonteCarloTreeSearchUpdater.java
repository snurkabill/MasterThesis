package vahy.impl.search.MCTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;

public class MonteCarloTreeSearchUpdater<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    implements TreeUpdater<TAction, TObservation, MonteCarloTreeSearchMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearchUpdater.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    @Override
    public void updateTree(SearchNode<TAction, TObservation, MonteCarloTreeSearchMetadata, TState> expandedNode) {
        int i = 0;
        double estimatedLeafReward = (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward()) + expandedNode.getSearchNodeMetadata().getCumulativeReward();
        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward);
        if(TRACE_ENABLED) {
            logger.trace("Traversing updated traversed [{}] tree levels", i);
        }
    }

    private void updateNode(SearchNode<TAction, TObservation, MonteCarloTreeSearchMetadata, TState> expandedNode, double estimatedLeafReward) {
        MonteCarloTreeSearchMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(estimatedLeafReward);
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getSumOfTotalEstimations() + estimatedLeafReward);
        }
        searchNodeMetadata.setExpectedReward(searchNodeMetadata.getSumOfTotalEstimations() / searchNodeMetadata.getVisitCounter());
    }
}
