package vahy.paper.tree;

import com.quantego.clp.CLPVariable;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SearchNode {

    private static long instanceCounter = 0;

    private final long nodeId = instanceCounter++;

    private final ImmutableStateImpl wrappedState;

    private SearchNode parent;
    private ActionType appliedParentAction;
    private DoubleScalarReward gainedReward;
    private DoubleScalarReward cumulativeReward;
    private double realRisk;
    private CLPVariable nodeProbabilityFlow;

    private int totalVisitCounter;  // sum over all b : N(s, b)
    private DoubleScalarReward estimatedReward; // in article V value
    private double estimatedRisk; // in article V value

    private final Map<ActionType, SearchNode> childMap = new HashMap<>();
    private final Map<ActionType, EdgeMetadata> edgeMetadataMap = new HashMap<>();

    private boolean alreadyEvaluated = false;

    public SearchNode(ImmutableStateImpl wrappedState, SearchNode parent, ActionType appliedParentAction, DoubleScalarReward gainedReward) {
        this.wrappedState = wrappedState;
        this.parent = parent;
        this.appliedParentAction = appliedParentAction;
        this.gainedReward = gainedReward;
        if(parent != null) {
            this.cumulativeReward = new DoubleScalarReward(parent.getCumulativeReward().getValue() + gainedReward.getValue());
        } else {
            this.cumulativeReward = new DoubleScalarReward(gainedReward.getValue());
        }
        this.realRisk = wrappedState.isAgentKilled() ? 1 : 0;
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

    public SearchNode getParent() {
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
        return isFinalNode() || childMap.entrySet().stream().noneMatch(x -> x.getValue().isAlreadyEvaluated());
    }

    public Map<ActionType, EdgeMetadata> getEdgeMetadataMap() {
        return edgeMetadataMap;
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
        DecimalFormat df = new DecimalFormat("#.####");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID: ");
        stringBuilder.append(nodeId);
        stringBuilder.append("\\nTotalVisit: ");
        stringBuilder.append(this.totalVisitCounter);
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward.getValue());
        stringBuilder.append("\\nEstimatedRew: ");
        stringBuilder.append(estimatedReward != null ? df.format(estimatedReward.getValue()) : null);
        stringBuilder.append("\\nEstimatedRisk: ");
        stringBuilder.append(df.format(getEstimatedRisk()));
        stringBuilder.append("\\nisLeaf: ");
        stringBuilder.append(isLeaf());
        stringBuilder.append("\\nisEvaluated: ");
        stringBuilder.append(isAlreadyEvaluated());
        stringBuilder.append("\\nNodeProbabilityFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? df.format(nodeProbabilityFlow.getSolution()) : null);
        return stringBuilder.toString();
    }

    public String toStringAsRootForGraphwiz() {
        DecimalFormat df = new DecimalFormat("#.####");
        LinkedList<SearchNode> queue = new LinkedList<>();
        queue.addFirst(this);

        StringBuilder string = new StringBuilder();
        String start = "digraph G {";
        String end = "}";

        string.append(start);
        while(!queue.isEmpty()) {
            SearchNode node = queue.poll();

            for (Map.Entry<ActionType, EdgeMetadata> entry : node.getEdgeMetadataMap().entrySet()) {
                SearchNode child = node.getChildMap().get(entry.getKey());
                queue.addLast(child);

                string.append("\"" + node.toStringForGraphwiz() + "\"");
                string.append(" -> ");
                string.append("\"" + child.toStringForGraphwiz() + "\"");
                string.append(" ");
                string.append("[ label = \"P(");
                string.append(entry.getKey());
                string.append(") = ");
                string.append(df.format(entry.getValue().getPriorProbability()));
                string.append("\" ]; \n");
            }
        }
        string.append(end);
        return string.toString();
    }

    public void setEstimatedRisk(double estimatedRisk) {
        this.estimatedRisk = estimatedRisk;
    }

    public double getEstimatedRisk() {
        return estimatedRisk;
    }

    public Map<ActionType, SearchNode> getChildMap() {
        return childMap;
    }

    public double getRealRisk() {
        return realRisk;
    }
}
