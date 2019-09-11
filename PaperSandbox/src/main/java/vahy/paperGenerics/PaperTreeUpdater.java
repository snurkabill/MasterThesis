package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.paperGenerics.metadata.PaperMetadata;

public class PaperTreeUpdater<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);

    @Override
    public void updateTree(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> expandedNode) {
        int i = 0;
        double estimatedLeafReward =
            expandedNode.getSearchNodeMetadata().getCumulativeReward() +
            (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward());

        double estimatedLeafRisk = expandedNode.isFinalNode() ?
            (expandedNode.getWrappedState().isRiskHit() ?
                1.0
                : 0.0)
            : expandedNode.getSearchNodeMetadata().getPredictedRisk();

        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward, estimatedLeafRisk);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> updatedNode,
                            double estimatedLeafReward,
                            double estimatedRisk) {
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
