package vahy.AlphaGo.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.nodeExpander.BaseNodeExpander;

import java.util.Arrays;
import java.util.Map;

public class AlphaGoNodeExpander {

    private static final Logger logger = LoggerFactory.getLogger(BaseNodeExpander.class);

    public void expandNode(AlphaGoSearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        if(node.getChildMap().isEmpty()) {
            innerExpandNode(node);
        }
        for (Map.Entry<ActionType, AlphaGoSearchNode> childEntry : node.getChildMap().entrySet()) {
            if(!childEntry.getValue().isFinalNode()) {
                innerExpandNode(childEntry.getValue());
            }
        }

//        ActionType[] allActions = node.isOpponentTurn() ? ActionType.environmentActions : ActionType.playerActions;

    }

    public void innerExpandNode(AlphaGoSearchNode node) {
        ActionType[] allActions = node.getWrappedState().getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allActions));

        if(node.getChildMap().size() != 0) {
            throw new IllegalStateException("Node was already expanded");
        }

        for (ActionType action : allActions) {
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation,
                                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = node.getWrappedState().applyAction(action);
            logger.trace("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            AlphaGoSearchNode newNode = new AlphaGoSearchNode((ImmutableStateImpl) stateRewardReturn.getState(), node, action, stateRewardReturn.getReward());
            AlphaGoEdgeMetadata edgeMetadata = new AlphaGoEdgeMetadata();
            node.getChildMap().put(action, newNode);
            node.getEdgeMetadataMap().put(action, edgeMetadata);
        }
    }
}
