package vahy.paper.tree.nodeExpander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.paper.tree.EdgeMetadata;
import vahy.paper.tree.SearchNode;

import java.util.Arrays;
import java.util.Map;

public class PaperNodeExpander implements NodeExpander {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeExpander.class);

    @Override
    public void expandNode(SearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        if(node.getChildMap().isEmpty()) {
            innerExpandNode(node);
        }
        for (Map.Entry<ActionType, SearchNode> childEntry : node.getChildMap().entrySet()) {
            if(!childEntry.getValue().isFinalNode()) {
                innerExpandNode(childEntry.getValue());
            }
        }

//        ActionType[] allActions = node.isOpponentTurn() ? ActionType.environmentActions : ActionType.playerActions;

    }

    public void innerExpandNode(SearchNode node) {
        ActionType[] allActions = node.getWrappedState().getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allActions));

        if(node.getChildMap().size() != 0) {
            throw new IllegalStateException("Node was already expanded");
        }

        for (ActionType action : allActions) {
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation,
                                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = node.getWrappedState().applyAction(action);
            logger.trace("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            SearchNode newNode = new SearchNode((ImmutableStateImpl) stateRewardReturn.getState(), node, action, stateRewardReturn.getReward());
            EdgeMetadata edgeMetadata = new EdgeMetadata();
            node.getChildMap().put(action, newNode);
            node.getEdgeMetadataMap().put(action, edgeMetadata);
        }
    }
}
