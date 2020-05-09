package vahy.impl.search.AlphaGo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVector;

public class AlphaGoTreeSearchUpdater<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends DoubleVector,
    TState extends State<TAction, TObservation, TState>>
    implements TreeUpdater<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoTreeSearchUpdater.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();


    @Override
    public void updateTree(SearchNode<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> expandedNode) {
        double estimatedLeafReward = (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward()) + expandedNode.getSearchNodeMetadata().getCumulativeReward();
        int i = 0;
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

    private void updateNode(SearchNode<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> expandedNode, double estimatedLeafReward) {
        AlphaGoNodeMetadata<TAction> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(estimatedLeafReward);
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getSumOfTotalEstimations() + estimatedLeafReward);
        }
        searchNodeMetadata.setExpectedReward(searchNodeMetadata.getSumOfTotalEstimations() / searchNodeMetadata.getVisitCounter());
    }
}
