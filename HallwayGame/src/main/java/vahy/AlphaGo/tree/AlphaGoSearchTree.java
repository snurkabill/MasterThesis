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

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -10);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -10);

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
    private double totalRiskAllowed;

    public AlphaGoSearchTree(
        AlphaGoSearchNode root,
        AlphaGoNodeSelector nodeSelector,
        AlphaGoNodeExpander nodeExpander,
        AlphaGoTreeUpdater treeUpdater,
        AlphaGoNodeEvaluator nodeEvaluationSimulator,
        OptimalFlowCalculator flowCalculator,
        double totalRiskAllowed) {
        this.root = root;
        this.nodeSelector = nodeSelector;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.nodeEvaluationSimulator = nodeEvaluationSimulator;
        this.nodeSelector.setNewRoot(root);
        this.flowCalculator = flowCalculator;
        this.totalRiskAllowed = totalRiskAllowed;
    }

    public DoubleScalarReward getRootEstimatedReward() {
        return root.getEstimatedReward();
    }

    public double getRootEstimatedRisk() {
        return root.getEstimatedRisk();
    }

    public boolean updateTree() {
        AlphaGoSearchNode selectedNodeForExpansion = nodeSelector.selectNextNode();
        if(selectedNodeForExpansion == null) {
            return false;
        }
        if(!selectedNodeForExpansion.isFinalNode()) {
            logger.trace("Selected node [{}] is not final node, expanding", selectedNodeForExpansion);
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
        if(root.getNodeProbabilityFlow() != null) {
            if(action.isPlayerAction()) {
                calculateNumericallyStableNewRiskThreshold(action);
            }
        }
        root = child;
        root.makeRoot();
        nodeSelector.setNewRoot(root);
        resetTreeStatistics();
        return new ImmutableStateRewardReturnTuple<>(root.getWrappedState(), reward);
    }

    private void calculateNumericallyStableNewRiskThreshold(ActionType appliedAction) {
        double totalRiskOfOtherActions = 0.0;
        for (Map.Entry<ActionType, AlphaGoSearchNode> entry : root.getChildMap().entrySet()) {
            if(entry.getKey() != appliedAction) {
                totalRiskOfOtherActions += calculateRiskContributionInSubTree(entry.getValue());
            }
        }
        double newRisk = calculateNewRiskValue(appliedAction, totalRiskOfOtherActions);
        if(newRisk < 0.0 || newRisk > 1.0) {
            throw new IllegalStateException("Risk out of bounds. Old risk [" + totalRiskAllowed + "] New risk calculated: [" + newRisk + "], risk of other actions: [" + totalRiskOfOtherActions + "], dividing probability: [" + root.getChildMap().get(appliedAction).getNodeProbabilityFlow().getSolution() + "]");
        }
        totalRiskAllowed = newRisk;
        isFlowOptimized = false;
    }

    private double calculateNewRiskValue(ActionType appliedAction, double totalRiskOfOtherActions) {
        double riskDiff = calculateNumericallyStableRiskDiff(totalRiskOfOtherActions);
        double actionProbability = calculateNumericallyStableActionProbability(root.getChildMap().get(appliedAction).getNodeProbabilityFlow().getSolution());
        if(actionProbability == 0.0) {
            logger.trace("Taken action with zero probability according to linear optimization. Setting risk to 1.0, since such action is probably taken due to exploration.");
            return 1.0;
        } else {
            return riskDiff / actionProbability;
        }
    }

    private double calculateNumericallyStableActionProbability(double calculatedProbability) {
        if(Math.abs(calculatedProbability) < NUMERICAL_PROBABILITY_TOLERANCE) {
            if (calculatedProbability != 0.0) {
                logger.trace("Rounding action probability to 0. This is done because linear optimization is not numerically stable");
            }
            return 0.0;
        } else if(Math.abs(calculatedProbability - 1.0) < NUMERICAL_PROBABILITY_TOLERANCE) {
            if(calculatedProbability != 1.0) {
                logger.trace("Rounding action probability to 1. This is done because linear optimization is not numerically stable");
            }
            return 1.0;
        } else if(calculatedProbability < 0.0) {
            throw new IllegalStateException("Probability cannot be lower than 0. This would cause program failure later in simulation");
        } else if(calculatedProbability > 1.0) {
            throw new IllegalStateException("Probability cannot be higher than 1. This would cause program failure later in simulation");
        }
        return calculatedProbability;
    }

    private double calculateNumericallyStableRiskDiff(double totalRiskOfOtherActions) {
        double riskDiff = (totalRiskAllowed - totalRiskOfOtherActions);
        if(Math.abs(riskDiff) < NUMERICAL_RISK_DIFF_TOLERANCE) {
            if(riskDiff != 0) {
                logger.trace("Rounding risk difference to 0. This si done because linear optimization is not numerically stable");
            }
            riskDiff = 0.0;
        } else if(riskDiff < 0.0) {
            throw new IllegalStateException("Risk difference is out of bounds. New risk difference [" + riskDiff + "]. Risk exceeds tolerated bound: [" + -NUMERICAL_RISK_DIFF_TOLERANCE + "]");
        }
        return riskDiff;
    }

    private double calculateRiskContributionInSubTree(AlphaGoSearchNode subTreeRoot) {
        double risk = 0;

        LinkedList<AlphaGoSearchNode> queue = new LinkedList<>();
        queue.addFirst(subTreeRoot);

        while(!queue.isEmpty()) {
            AlphaGoSearchNode node = queue.poll();
            if(node.isLeaf()) {
                if(node.getWrappedState().isAgentKilled()) {
                    risk += node.getNodeProbabilityFlow().getSolution();
                }
            } else {
                for (Map.Entry<ActionType, AlphaGoSearchNode> entry : node.getChildMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

    public void optimizeFlow() {
        if(!isFlowOptimized) {
            optimizedObjectiveValue = flowCalculator.calculateFlow(root, totalRiskAllowed);
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

