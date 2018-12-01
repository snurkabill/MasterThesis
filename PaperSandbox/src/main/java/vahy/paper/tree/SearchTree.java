package vahy.paper.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.paper.tree.nodeExpander.NodeExpander;
import vahy.paper.tree.treeUpdater.TreeUpdater;

import java.util.LinkedList;
import java.util.Map;

public class SearchTree {

    private static final Logger logger = LoggerFactory.getLogger(SearchTree.class);

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -10);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -10);
    public static final double NUMERICAL_ACTION_RISK_TOLERANCE = Math.pow(10, -10);

    private SearchNode root;
    private final NodeSelector nodeSelector;
    private final NodeExpander nodeExpander;
    private final TreeUpdater treeUpdater;
    private final NodeEvaluator nodeEvaluationSimulator;

    private int totalNodesExpanded = 0;
    private int totalNodesCreated = 0; // should be 1 for root
    private int maxBranchingFactor = 0;

    private OptimalFlowCalculator flowCalculator;
    private boolean isFlowOptimized = false;
    private double optimizedObjectiveValue;
    private double totalRiskAllowed;

    public SearchTree(
        SearchNode root,
        NodeSelector nodeSelector,
        NodeExpander nodeExpander,
        TreeUpdater treeUpdater,
        NodeEvaluator nodeEvaluationSimulator,
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

    public DoubleReward getRootEstimatedReward() {
        return root.getEstimatedReward();
    }

    public double getRootEstimatedRisk() {
        return root.getEstimatedRisk();
    }

    public boolean updateTree() {
        SearchNode selectedNodeForExpansion = nodeSelector.selectNextNode();
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

    public StateRewardReturn<ActionType, DoubleReward, DoubleVector, ImmutableStateImpl> applyAction(ActionType action) {
        if(root.isFinalNode()) {
            throw new IllegalStateException("Can't apply action [" + action +"] on final state");
        }
        if(root.isLeaf()) {
            logger.debug("Trying to apply action on not expanded tree branch. Forcing expansion.");
            expandNode(root);
        }
        SearchNode child = root.getChildMap().get(action);
        DoubleReward reward = child.getGainedReward();
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
        double riskOfOtherActions = calculateNumericallyStableRiskOfAnotherActions(appliedAction);
        double riskDiff = calculateNumericallyStableRiskDiff(riskOfOtherActions);
        double actionProbability = calculateNumericallyStableActionProbability(root.getChildMap().get(appliedAction).getNodeProbabilityFlow().getSolution());
        double newRisk = calculateNewRiskValue(riskDiff, actionProbability, riskOfOtherActions, appliedAction);
        totalRiskAllowed = newRisk;
        isFlowOptimized = false;
    }

    private double calculateNumericallyStableRiskOfAnotherActions(ActionType appliedAction) {
        double riskOfOtherActions = 0.0;
        for (Map.Entry<ActionType, SearchNode> entry : root.getChildMap().entrySet()) {
            if(entry.getKey() != appliedAction) {
                riskOfOtherActions += calculateRiskContributionInSubTree(entry.getValue());
            }
        }

        if(Math.abs(riskOfOtherActions) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            if (riskOfOtherActions != 0.0) {
                logger.trace("Rounding risk of other actions to 0. This is done because linear optimization is not numerically stable");
            }
            return 0.0;
        } else if(Math.abs(riskOfOtherActions - 1.0) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            if(riskOfOtherActions != 1.0) {
                logger.trace("Rounding risk of other actions to 1. This is done because linear optimization is not numerically stable");
            }
            return 1.0;
        } else if(riskOfOtherActions < 0.0) {
            throw new IllegalStateException("Risk of other actions cannot be lower than 0. This would cause program failure later in simulation");
        } else if(riskOfOtherActions > 1.0) {
            throw new IllegalStateException("Risk of other actions cannot be higher than 1. This would cause program failure later in simulation");
        }
        return riskOfOtherActions;

    }

    private double calculateNewRiskValue(double riskDiff, double actionProbability, double riskOfOtherActions, ActionType appliedAction) {
        if(actionProbability == 0.0) {
            logger.trace("Taken action with zero probability according to linear optimization. Setting risk to 1.0, since such action is probably taken due to exploration.");
            return 1.0;
        } else {
            double newRisk = riskDiff / actionProbability;
            if((newRisk < -NUMERICAL_RISK_DIFF_TOLERANCE) || (newRisk - 1.0 > NUMERICAL_RISK_DIFF_TOLERANCE)) {
                throw new IllegalStateException(
                    "Risk out of bounds. " +
                        "Old risk [" + totalRiskAllowed + "]. " +
                        "Risk diff numerically stabilised: [" +  riskDiff + "] " +
                        "New risk calculated: [" + newRisk + "], " +
                        "Numerically stable risk of other actions: [" + riskOfOtherActions + "], " +
                        "Dividing probability: [" + root.getChildMap().get(appliedAction).getNodeProbabilityFlow().getSolution() + "], " +
                        "Numerically stabilised dividing probability: [" + actionProbability + "]");
            }
            if(newRisk > 1.0) {
                logger.trace("Rounding new risk to 1.0.");
                return 1.0;
            }
            if(newRisk < 0.0) {
                logger.trace("Rounding newRisk to 0.0");
                return 0.0;
            }
            return newRisk;
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

    private double calculateRiskContributionInSubTree(SearchNode subTreeRoot) {
        double risk = 0;

        LinkedList<SearchNode> queue = new LinkedList<>();
        queue.addFirst(subTreeRoot);

        while(!queue.isEmpty()) {
            SearchNode node = queue.poll();
            if(node.isLeaf()) {
                if(node.getWrappedState().isAgentKilled()) {
                    risk += node.getNodeProbabilityFlow().getSolution();
                }
            } else {
                for (Map.Entry<ActionType, SearchNode> entry : node.getChildMap().entrySet()) {
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

    public DoubleVector getObservation() {
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

    public SearchNode getRoot() {
        return root;
    }

    private void expandNode(SearchNode selectedNodeForExpansion) {
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
        return root.toStringAsRootForGraphwiz();
    }
}

