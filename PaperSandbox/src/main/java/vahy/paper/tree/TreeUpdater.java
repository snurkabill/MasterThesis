package vahy.paper.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.ActionType;

public class TreeUpdater {

    private static final Logger logger = LoggerFactory.getLogger(TreeUpdater.class);


    public void updateTree(SearchNode expandedNode) {
        int i = 0;
        double estimatedLeafReward = (expandedNode.isFinalNode() ?
            0.0d :
            expandedNode.getEstimatedReward().getValue())
            + expandedNode.getCumulativeReward().getValue();
        double estimatedLeafRisk = expandedNode.isFinalNode() ?
            expandedNode.getWrappedState().isAgentKilled() ?
                1.0
                : 0.0
            : expandedNode.getEstimatedRisk();

        while (!expandedNode.isRoot()) {
            SearchNode parent = expandedNode.getParent();
            ActionType appliedAction = expandedNode.getAppliedParentAction();

            expandedNode.setTotalVisitCounter(expandedNode.getTotalVisitCounter() + 1);
            EdgeMetadata metadata = parent.getEdgeMetadataMap().get(appliedAction);
            metadata.setVisitCount(metadata.getVisitCount() + 1);

            metadata.setTotalActionValue(metadata.getTotalActionValue() + estimatedLeafReward);
            metadata.setTotalRiskValue(metadata.getTotalRiskValue() + estimatedLeafRisk);
            metadata.setMeanActionValue(metadata.getTotalActionValue() / metadata.getVisitCount());
            metadata.setMeanRiskValue(metadata.getTotalRiskValue() / metadata.getVisitCount());

            expandedNode = parent;
            i++;
        }
        expandedNode.setTotalVisitCounter(expandedNode.getTotalVisitCounter() + 1);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

}
