package vahy.AlphaGo.tree;

import com.quantego.clp.CLPVariable;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.HashMap;
import java.util.Map;

public class AlphaGoSearchNode {

    private static long instanceCounter = 0;

    private final long nodeId = instanceCounter++;

    private final ImmutableStateImpl wrappedState;

    private AlphaGoSearchNode parent;
    private ActionType appliedParentAction;
    private DoubleScalarReward gainedReward;
    private DoubleScalarReward cumulativeReward;
    private CLPVariable nodeProbabilityFlow;

    private int totalVisitCounter;  // sum over all b : N(s, b)
    private DoubleScalarReward estimatedReward; // in article V value

    private final Map<ActionType, AlphaGoEdgeMetadata> childMap = new HashMap<>();

    private boolean alreadyEvaluated = false;

    public AlphaGoSearchNode(ImmutableStateImpl wrappedState, AlphaGoSearchNode parent, ActionType appliedParentAction, DoubleScalarReward gainedReward) {
        this.wrappedState = wrappedState;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
        this.gainedReward = gainedReward;
        if(parent != null) {
            this.cumulativeReward = new DoubleScalarReward(parent.getCumulativeReward().getValue() + gainedReward.getValue());
        } else {
            this.cumulativeReward = new DoubleScalarReward(gainedReward.getValue());
        }
    }

    public CLPVariable getNodeProbabilityFlow() {
        return nodeProbabilityFlow;
    }

    public void setNodeProbabilityFlow(CLPVariable nodeProbabilityFlow) {
        this.nodeProbabilityFlow = nodeProbabilityFlow;
    }

    public boolean isAlreadyEvaluated() {
        return alreadyEvaluated;
    }

    public void setEvaluated() {
        alreadyEvaluated = true;
    }

    public void setEstimatedReward(DoubleScalarReward estimatedReward) {
        this.estimatedReward = estimatedReward;
    }

    public DoubleScalarReward getGainedReward() {
        return gainedReward;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public ImmutableStateImpl getWrappedState() {
        return wrappedState;
    }

    public AlphaGoSearchNode getParent() {
        return parent;
    }

    public ActionType getAppliedParentAction() {
        return appliedParentAction;
    }

    public int getTotalVisitCounter() {
        return totalVisitCounter;
    }

    public DoubleScalarReward getEstimatedReward() {
        return estimatedReward;
    }

    public boolean isLeaf() {
        return isFinalNode() || childMap.isEmpty();
    }

    public Map<ActionType, AlphaGoEdgeMetadata> getChildMap() {
        return childMap;
    }

    public boolean isFinalNode() {
        return wrappedState.isFinalState();
    }

    public void makeRoot() {
        parent = null;
        appliedParentAction = null;
    }

    public boolean isOpponentTurn() {
        return wrappedState.isOpponentTurn();
    }

    public boolean isAgentTurn() {
        return wrappedState.isAgentTurn();
    }

    public void setTotalVisitCounter(int totalVisitCounter) {
        this.totalVisitCounter = totalVisitCounter;
    }

    public DoubleScalarReward getCumulativeReward() {
        return cumulativeReward;
    }

    public String toStringForGraphwiz() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID: ");
        stringBuilder.append(nodeId);
        stringBuilder.append("\\nTotalVisit: ");
        stringBuilder.append(this.totalVisitCounter);
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward.getValue());
        stringBuilder.append("\\nEstimatedRew: ");
        stringBuilder.append(estimatedReward != null ? estimatedReward.getValue() : null);
        stringBuilder.append("\\nNodeProbabilityFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? nodeProbabilityFlow.getSolution() : null);
        return stringBuilder.toString();
    }
}
