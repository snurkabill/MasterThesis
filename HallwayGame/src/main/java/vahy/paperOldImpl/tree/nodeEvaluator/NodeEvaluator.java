package vahy.paperOldImpl.tree.nodeEvaluator;

import vahy.environment.HallwayAction;
import vahy.paperOldImpl.tree.SearchNode;

import java.util.Map;

public abstract class NodeEvaluator {

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    public void evaluateNode(SearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be evaluated");
        }
        if(!node.isAlreadyEvaluated()) {
            innerEvaluateNode(node);
        }
        for (Map.Entry<HallwayAction, SearchNode> childEntry : node.getChildMap().entrySet()) {
            if(!childEntry.getValue().isFinalNode()) {
                innerEvaluateNode(childEntry.getValue());
            }
        }
    }

    protected abstract void innerEvaluateNode(SearchNode node);

}
