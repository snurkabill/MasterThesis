package vahy.impl.search.AlphaGo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

public class AlphaGoTreeSearchUpdater<
    TAction extends Action,
    TObservation extends DoubleVectorialObservation,
    TState extends State<TAction, DoubleScalarReward, TObservation, TState>>
    implements TreeUpdater<TAction, DoubleScalarReward, TObservation, AlphaGoNodeMetadata<TAction, DoubleScalarReward>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoTreeSearchUpdater.class);


    @Override
    public void updateTree(SearchNode<TAction, DoubleScalarReward, TObservation, AlphaGoNodeMetadata<TAction, DoubleScalarReward>, TState> expandedNode) {
        double estimatedLeafReward = (expandedNode.isFinalNode() ? 0.0d : expandedNode.getSearchNodeMetadata().getPredictedReward().getValue()) + expandedNode.getSearchNodeMetadata().getCumulativeReward().getValue();
        int i = 0;
        while (!expandedNode.isRoot()) {
            updateNode(expandedNode, estimatedLeafReward);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode, estimatedLeafReward);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, DoubleScalarReward, TObservation, AlphaGoNodeMetadata<TAction, DoubleScalarReward>, TState> expandedNode, double estimatedLeafReward) {
        AlphaGoNodeMetadata<TAction, DoubleScalarReward> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleScalarReward(estimatedLeafReward));
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleScalarReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() + estimatedLeafReward));
        }
        searchNodeMetadata.setExpectedReward(new DoubleScalarReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() / searchNodeMetadata.getVisitCounter()));
    }
}
