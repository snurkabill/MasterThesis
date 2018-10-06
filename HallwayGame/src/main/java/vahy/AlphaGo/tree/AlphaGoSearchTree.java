package vahy.AlphaGo.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.tree.SearchTreeImpl;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Map;

public class AlphaGoSearchTree {

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeImpl.class);

    private AlphaGoSearchNode root;
    private final AlphaGoNodeSelector nodeSelector;
    private final AlphaGoNodeExpander nodeExpander;
    private final AlphaGoTreeUpdater treeUpdater;
    private final AlphaGoNodeEvaluator nodeEvaluationSimulator;

    private int totalNodesExpanded = 0;
    private int totalNodesCreated = 0; // should be 1 for root
    private int maxBranchingFactor = 0;

    private OptimalFlowCalculator flowCalculator;
    private boolean isFlowOptimized = false;
    private double optimizedObjectiveValue;

    public AlphaGoSearchTree(
        AlphaGoSearchNode root,
        AlphaGoNodeSelector nodeSelector,
        AlphaGoNodeExpander nodeExpander,
        AlphaGoTreeUpdater treeUpdater,
        AlphaGoNodeEvaluator nodeEvaluationSimulator,
        OptimalFlowCalculator flowCalculator) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.nodeEvaluationSimulator = nodeEvaluationSimulator;
        this.nodeSelector.setNewRoot(root);
        this.flowCalculator = flowCalculator;
    }

    public DoubleScalarReward getRootEstimatedReward() {
        return root.getEstimatedReward();
    }

    public double getRootEstimatedRisk() {
        return root.getEstimatedRisk();
    }

    public DoubleScalarReward getRootEstimatedRewardAfterFlowOptimisation() {
        if(!isFlowOptimized) {
            optimizeFlow();
        }
        return new DoubleScalarReward(optimizedObjectiveValue);
    }

    public boolean updateTree() {
        AlphaGoSearchNode selectedNodeForExpansion = nodeSelector.selectNextNode();
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            logger.debug("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
            expandNode(selectedNodeForExpansion);
            nodeEvaluationSimulator.evaluateNode(selectedNodeForExpansion);
            isFlowOptimized = false; // possibly needed after select in general
        }
        treeUpdater.updateTree(selectedNodeForExpansion);
        return true;
    }

    public ActionType[] getAllPossibleActions() {
        return this.root.getWrappedState().getAllPossibleActions();
    }

    public StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, ImmutableStateImpl> applyAction(ActionType action) {
        if(root.isFinalNode()) {
            throw new IllegalStateException("Can't apply action [" + action +"] on final state");
        }
        if(root.isLeaf()) {
            logger.debug("Trying to apply action on not expanded tree branch. Forcing expansion.");
            expandNode(root);
        }
        AlphaGoSearchNode child = root.getChildMap().get(action);
        DoubleScalarReward reward = child.getGainedReward();
        isFlowOptimized = false;
        root = child;
        root.makeRoot();
        nodeSelector.setNewRoot(root);
        resetTreeStatistics();
        return new ImmutableStateRewardReturnTuple<>(root.getWrappedState(), reward);
    }

    public void optimizeFlow() {
        if(!isFlowOptimized) {
            optimizedObjectiveValue = flowCalculator.calculateFlow(root);
            isFlowOptimized = true;
        }
    }

    public DoubleVectorialObservation getObservation() {
        return root.getWrappedState().getObservation();
    }

    public String readableStringRepresentation() {
        return root.getWrappedState().readableStringRepresentation();
    }

    public boolean isOpponentTurn() {
        return root.isOpponentTurn();
    }

    public boolean isFinalState() {
        return root.isFinalNode();
    }

    public AlphaGoSearchNode getRoot() {
        return root;
    }

    private void expandNode(AlphaGoSearchNode selectedNodeForExpansion) {
        nodeExpander.expandNode(selectedNodeForExpansion);
        totalNodesExpanded++;
    }

    private void resetTreeStatistics() {
        totalNodesCreated = 0;
        totalNodesExpanded = 0;
        maxBranchingFactor = Integer.MIN_VALUE;
    }

    public int getTotalNodesExpanded() {
        return totalNodesExpanded;
    }

    public int getTotalNodesCreated() {
        return totalNodesCreated;
    }

    public int getMaxBranchingFactor() {
        return maxBranchingFactor;
    }

    public double calculateAverageBranchingFactor() {
        return totalNodesCreated / (double) totalNodesExpanded;
    }

    public String toStringForGraphwiz() {
        DecimalFormat df = new DecimalFormat("#.####");
        LinkedList<AlphaGoSearchNode> queue = new LinkedList<>();
        queue.addFirst(root);

        StringBuilder string = new StringBuilder();
        String start = "digraph G {";
        String end = "}";

        string.append(start);
        while(!queue.isEmpty()) {
            AlphaGoSearchNode node = queue.poll();

            for (Map.Entry<ActionType, AlphaGoEdgeMetadata> entry : node.getEdgeMetadataMap().entrySet()) {
                AlphaGoSearchNode child = node.getChildMap().get(entry.getKey());
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
}

