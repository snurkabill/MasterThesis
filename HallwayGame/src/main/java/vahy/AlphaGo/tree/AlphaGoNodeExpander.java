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

public class AlphaGoNodeExpander {

    private static final Logger logger = LoggerFactory.getLogger(BaseNodeExpander.class);

    public void expandNode(AlphaGoSearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }

//        ActionType[] allActions = node.isOpponentTurn() ? ActionType.environmentActions : ActionType.playerActions;
        ActionType[] allActions = node.getWrappedState().getAllPossibleActions();
        logger.debug("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allActions));

        for (ActionType action : allActions) {
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation,
                                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = node.getWrappedState().applyAction(action);
            logger.debug("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            AlphaGoSearchNode newNode = new AlphaGoSearchNode((ImmutableStateImpl) stateRewardReturn.getState(), node, action, stateRewardReturn.getReward());
            AlphaGoEdgeMetadata edgeMetadata = new AlphaGoEdgeMetadata();
            edgeMetadata.setChild(newNode);
            node.getChildMap().put(action, edgeMetadata);
        }
    }
}
