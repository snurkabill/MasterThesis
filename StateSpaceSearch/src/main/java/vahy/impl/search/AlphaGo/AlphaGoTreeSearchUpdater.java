package vahy.impl.search.AlphaGo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public class AlphaGoTreeSearchUpdater<
    TAction extends Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends DoubleVector,
    TState extends State<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, TState>>
    implements TreeUpdater<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction, DoubleReward>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoTreeSearchUpdater.class);


    @Override
    public void updateTree(SearchNode<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction, DoubleReward>, TState> expandedNode) {
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

    private void updateNode(SearchNode<TAction, DoubleReward, TPlayerObservation, TOpponentObservation, AlphaGoNodeMetadata<TAction, DoubleReward>, TState> expandedNode, double estimatedLeafReward) {
        AlphaGoNodeMetadata<TAction, DoubleReward> searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();
        if(searchNodeMetadata.getVisitCounter() == 1) {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(estimatedLeafReward));
        } else {
            searchNodeMetadata.setSumOfTotalEstimations(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() + estimatedLeafReward));
        }
        searchNodeMetadata.setExpectedReward(new DoubleReward(searchNodeMetadata.getSumOfTotalEstimations().getValue() / searchNodeMetadata.getVisitCounter()));
    }
}
