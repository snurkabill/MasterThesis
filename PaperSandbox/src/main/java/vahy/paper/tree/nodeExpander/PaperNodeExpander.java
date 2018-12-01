package vahy.paper.tree.nodeExpander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paper.tree.EdgeMetadata;
import vahy.paper.tree.SearchNode;

import java.util.Arrays;
import java.util.Map;

public class PaperNodeExpander implements NodeExpander {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeExpander.class);

    private int nodesExpandedCount = 0;

    @Override
    public void expandNode(SearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        if(node.getChildMap().isEmpty()) {
            innerExpandNode(node);
        }
        for (Map.Entry<HallwayAction, SearchNode> childEntry : node.getChildMap().entrySet()) {
            if(!childEntry.getValue().isFinalNode()) {
                innerExpandNode(childEntry.getValue());
            }
        }
    }

    @Override
    public int getNodesExpandedCount() {
        return nodesExpandedCount;
    }

    public void innerExpandNode(SearchNode node) {
        nodesExpandedCount++;
        HallwayAction[] allActions = node.getWrappedState().getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", node, Arrays.toString(allActions));

        if(node.getChildMap().size() != 0) {
            throw new IllegalStateException("Node was already expanded");
        }

        for (HallwayAction action : allActions) {
            StateRewardReturn<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> stateRewardReturn = node.getWrappedState().applyAction(action);
            logger.trace("Expanding node [{}] with action [{}] resulting in reward [{}]", node, action, stateRewardReturn.getReward().toPrettyString());
            SearchNode newNode = new SearchNode(stateRewardReturn.getState(), node, action, stateRewardReturn.getReward());
            EdgeMetadata edgeMetadata = new EdgeMetadata();
            node.getChildMap().put(action, newNode);
            node.getEdgeMetadataMap().put(action, edgeMetadata);
        }
    }
}
