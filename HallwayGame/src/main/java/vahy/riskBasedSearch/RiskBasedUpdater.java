package vahy.riskBasedSearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperTreeUpdater;

public class RiskBasedUpdater<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, TState>>
    implements TreeUpdater<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, DoubleReward>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);

    @Override
    public void updateTree(SearchNode<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, DoubleReward>, TState> expandedNode) {
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

        PaperMetadata<TAction, DoubleReward> leafSearchNodeMetadata =  expandedNode.getSearchNodeMetadata();
        leafSearchNodeMetadata.increaseVisitCounter();
        leafSearchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(estimatedLeafReward));
        leafSearchNodeMetadata.setSumOfRisk(estimatedLeafRisk);

        if(!expandedNode.isRoot()) {
            expandedNode = expandedNode.getParent();

            while (!expandedNode.isRoot()) {
                updateNode(expandedNode);
                expandedNode = expandedNode.getParent();
                i++;
            }
        } else if(expandedNode.isRoot() && expandedNode.getChildNodeStream().anyMatch(x -> x.getSearchNodeMetadata().getVisitCounter() > 0)) {
            updateNode(expandedNode);
        }

        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction, DoubleReward>, TState> expandedNode) {
        PaperMetadata<TAction, DoubleReward> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        double weightedRisk = expandedNode
            .getChildNodeStream()
            .filter(x -> x.getSearchNodeMetadata().getVisitCounter() > 0)
            .mapToDouble(x -> x.getSearchNodeMetadata().getPriorProbability() * x.getSearchNodeMetadata().getSumOfRisk())
            .min().orElseThrow(() -> new IllegalStateException("Min does not exist"));
        searchNodeMetadata.setSumOfRisk(weightedRisk);
        double weightedReward = expandedNode
            .getChildNodeStream()
            .filter(x -> x.getSearchNodeMetadata().getVisitCounter() > 0)
            .mapToDouble(x -> x.getSearchNodeMetadata().getPriorProbability() * x.getSearchNodeMetadata().getSumOfTotalEstimations().getValue())
            .sum();
        searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(weightedReward));
    }

}
