package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.environment.state.PaperState;
import vahy.impl.model.reward.DoubleReward;

public class PaperTreeUpdater<
    TAction extends Action,
    TObservation extends Observation,
    TState extends PaperState<TAction, DoubleReward, TObservation, TState>>
    implements TreeUpdater<TAction, DoubleReward, TObservation, PaperMetadata<TAction, DoubleReward>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);

    @Override
    public void updateTree(SearchNode<TAction, DoubleReward, TObservation, PaperMetadata<TAction, DoubleReward>, TState> expandedNode) {
        int i = 0;
        double estimatedLeafReward = (expandedNode.isFinalNode() ?
            0.0d :
            expandedNode.getSearchNodeMetadata().getPredictedReward().getValue())
            + expandedNode.getSearchNodeMetadata().getCumulativeReward().getValue();
        double estimatedLeafRisk = expandedNode.isFinalNode() ?
            expandedNode.getWrappedState().isRiskHit() ?
                1.0
                : 0.0
            : expandedNode.getSearchNodeMetadata().getPredictedRisk();

        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, DoubleReward, TObservation, PaperMetadata<TAction, DoubleReward>, TState> expandedNode,
                            double estimatedLeafReward,
                            double estimatedRisk) {
        PaperMetadata<TAction, DoubleReward> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(estimatedLeafReward));
            searchNodeMetadata.setSumOfRisk(estimatedRisk);
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() + estimatedLeafReward));
            searchNodeMetadata.setSumOfRisk(searchNodeMetadata.getSumOfRisk() + estimatedRisk);
        }
        searchNodeMetadata.setExpectedReward(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() / searchNodeMetadata.getVisitCounter()));
        searchNodeMetadata.setPredictedRisk(searchNodeMetadata.getSumOfRisk() / searchNodeMetadata.getVisitCounter());
    }
}
