package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.paperGenerics.metadata.PaperMetadata;

public class PaperTreeUpdater<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends PaperState<TAction, TObservation, TState>>
    implements TreeUpdater<TAction, TObservation, PaperMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private double[] resolveRisk(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> expandedNode) {
        if(expandedNode.isFinalNode()) {
            var riskVector = ((PaperStateWrapper<TAction, TObservation, TState>) expandedNode.getStateWrapper()).getRiskVector();
            var asDoubles = new double[riskVector.length];
            for (int i = 0; i < riskVector.length; i++) {
                asDoubles[i] = riskVector[i] ? 1.0 : 0.0;
            }
            return asDoubles;
        } else {
            return expandedNode.getSearchNodeMetadata().getExpectedRisk();
        }
    }

    @Override
    public void updateTree(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> expandedNode) {
        int i = 0;
        var stateWrapper = expandedNode.getStateWrapper();
        var nodeMetadata = expandedNode.getSearchNodeMetadata();
        var cumulativeReward = nodeMetadata.getCumulativeReward();
        double[] estimatedLeafReward = stateWrapper.isFinalState() ?
            cumulativeReward :
            DoubleVectorRewardAggregator.aggregate(nodeMetadata.getExpectedReward(), cumulativeReward);

        double[] estimatedLeafRisk = resolveRisk(expandedNode);

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

    private void updateNode(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> expandedNode, double[] estimatedLeafReward, double[] estimatedLeafRisk) {
        PaperMetadata<TAction> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();

        var expectedRewards = searchNodeMetadata.getExpectedReward();
        var totalRewardEstimations = searchNodeMetadata.getSumOfTotalEstimations();
        var totalRiskEstimations = searchNodeMetadata.getSumOfRisk();

        if(searchNodeMetadata.getVisitCounter() == 1) {
            System.arraycopy(expectedRewards, 0, totalRewardEstimations, 0, estimatedLeafReward.length);
            System.arraycopy(estimatedLeafRisk, 0, totalRiskEstimations, 0, estimatedLeafReward.length);
        } else {
            var cumulativeRewards = searchNodeMetadata.getCumulativeReward();
            for (int i = 0; i < totalRewardEstimations.length; i++) {
                totalRewardEstimations[i] += estimatedLeafReward[i] - cumulativeRewards[i];
                totalRiskEstimations[i] += estimatedLeafRisk[i];
            }
            var expectedRisks = searchNodeMetadata.getExpectedRisk();
            var visitCounter = searchNodeMetadata.getVisitCounter();
            for (int i = 0; i < expectedRewards.length; i++) {
                expectedRewards[i] = totalRewardEstimations[i] / visitCounter;
                expectedRisks[i] = totalRiskEstimations[i] / visitCounter;
            }
        }
    }
}
