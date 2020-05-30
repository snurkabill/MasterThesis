package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.paperGenerics.metadata.PaperMetadata;

public class PaperTreeUpdater<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends PaperState<TAction, TObservation, TState>>
    implements TreeUpdater<TAction, TObservation, PaperMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    @Override
    public void updateTree(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> expandedNode) {
        int i = 0;
        // TODO: THIS IS DIRTY
        int policyId = expandedNode.getStateWrapper().getinGameEntityIdWrapper();

        double estimatedLeafReward = expandedNode.getSearchNodeMetadata().getCumulativeReward() + (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward());
        double estimatedLeafRisk = expandedNode.isFinalNode() ? (expandedNode.getStateWrapper().getWrappedState().isRiskHit(policyId) ? 1.0 : 0.0) : expandedNode.getSearchNodeMetadata().getPredictedRisk();
        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
        if(TRACE_ENABLED) {
            logger.trace("Traversing updated traversed [{}] tree levels", i);
        }
    }

    private void updateNode(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> updatedNode, double estimatedLeafReward, double estimatedRisk) {
        if(TRACE_ENABLED) {
            logger.trace("Updating search node: [{}]", updatedNode);
        }
        PaperMetadata<TAction> searchNodeMetadata = updatedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();

        if(updatedNode.isFinalNode()) {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(0.0);
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            }
        } else {
            if(searchNodeMetadata.getVisitCounter() == 1) {
                searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getPredictedReward());
                searchNodeMetadata.setSumOfRisk(estimatedRisk);
            } else {
                searchNodeMetadata.setSumOfTotalEstimations(searchNodeMetadata.getSumOfTotalEstimations() + (estimatedLeafReward - searchNodeMetadata.getCumulativeReward()));
                searchNodeMetadata.setSumOfRisk(searchNodeMetadata.getSumOfRisk() + estimatedRisk);
            }
            searchNodeMetadata.setExpectedReward(searchNodeMetadata.getSumOfTotalEstimations() / searchNodeMetadata.getVisitCounter());
            searchNodeMetadata.setPredictedRisk(searchNodeMetadata.getSumOfRisk() / searchNodeMetadata.getVisitCounter());
        }
    }
}
