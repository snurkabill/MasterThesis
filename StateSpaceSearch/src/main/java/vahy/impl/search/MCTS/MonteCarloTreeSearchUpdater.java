package vahy.impl.search.MCTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.reward.DoubleScalarReward;

public class MonteCarloTreeSearchUpdater<
    TAction extends Action,
    TObservation extends Observation,
    TState extends State<TAction, DoubleScalarReward, TObservation, TState>>
    implements TreeUpdater<TAction, DoubleScalarReward, TObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearchUpdater.class);

    @Override
    public void updateTree(SearchNode<TAction, DoubleScalarReward, TObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TState> expandedNode) {
        int i = 0;
        double estimatedLeafReward = (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward().getValue()) + expandedNode.getSearchNodeMetadata().getCumulativeReward().getValue();
        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, DoubleScalarReward, TObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TState> expandedNode, double estimatedLeafReward) {
        MonteCarloTreeSearchMetadata<DoubleScalarReward> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleScalarReward(estimatedLeafReward));
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleScalarReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() + estimatedLeafReward));
        }
        searchNodeMetadata.setExpectedReward(new DoubleScalarReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() / searchNodeMetadata.getVisitCounter()));
    }
}
