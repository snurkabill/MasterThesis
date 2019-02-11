package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.paperGenerics.policy.OptimalFlowCalculator;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RiskAverseSearchTree<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends SearchTreeImpl<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(RiskAverseSearchTree.class);

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_ACTION_RISK_TOLERANCE = Math.pow(10, -13);

    private final OptimalFlowCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> optimalFlowCalculator = new OptimalFlowCalculator<>(); // pass in constructor
    private SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> latestTreeWithPlayerOnTurn = null;
    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;
    private double parentPathProbability = 0.0;
    private double acumulatedRiskOfOtherActions = 0.0;

    public RiskAverseSearchTree(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root,
                                NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                double totalRiskAllowed) {
        super(root, nodeSelector, treeUpdater, nodeEvaluator);
        this.totalRiskAllowed = totalRiskAllowed;
    }

    public void optimizeFlow() {
        if(!isFlowOptimized) {
            boolean optimalSolutionExists = optimalFlowCalculator.calculateFlow(getRoot(), totalRiskAllowed);
            if(!optimalSolutionExists) {
                logger.error("Solution to flow optimisation does not exist. Setting allowed risk to 1.0");
                totalRiskAllowed = 1.0;
            }
            isFlowOptimized = true;
        }
    }

    public List<TAction> getAllowedActionsForExploration() {
        TAction[] actions = getRoot().getAllPossibleActions();
        var allowedActions = new LinkedList<TAction>();
        for (TAction action : actions) {
            if (calculateRiskOfOpponentNodes(getRoot().getChildNodeMap().get(action)) <= totalRiskAllowed) {
                allowedActions.add(action);
            }
        }
        return allowedActions;
    }

    private double calculateRiskOfOpponentNodes(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        if(node.isFinalNode()) {
            return node.getWrappedState().isRiskHit() ?  1.0 : 0.0;
        }
        if(node.isPlayerTurn()) {
            return 0.0;
        }
        if(node.isLeaf()) {
            throw new IllegalStateException("Risk can't be calculated from leaf nodes which are not player turns. Tree should be expanded up to player or final nodes");
        }
        return node
            .getChildNodeStream()
            .map(x -> new ImmutableTuple<>(x, x.getSearchNodeMetadata().getPriorProbability()))
            .mapToDouble(x -> calculateRiskOfOpponentNodes(x.getFirst()) * x.getSecond())
            .sum();
    }

    @Override
    public StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
        checkApplicableAction(action);
        // TODO make general in applicable action
        if(!getRoot().getChildNodeMap().containsKey(action)) {
            throw new IllegalStateException("Action [" + action + "] is invalid and cannot be applied to current policy state");
        }
        if(action.isPlayerAction()) {
            latestTreeWithPlayerOnTurn = this.getRoot();
        }
        logger.debug("Old Global risk: [{}] and applying action: [{}] with probability: [{}]", totalRiskAllowed, action, action.isPlayerAction()
            ? getPlayerActionProbability(action)
            : getOpponentActionProbability(action));
        logger.debug("Action probability distribution: [{}]", getRoot()
            .getChildNodeStream()
            .map(x -> action.isPlayerAction() ? getPlayerActionProbability(x.getAppliedAction()) : getOpponentActionProbability(x.getAppliedAction()))
            .map(Object::toString)
            .reduce((s, s2) -> s + ", " + s2)
            .orElseThrow(() -> new IllegalStateException("Reduce op does not exists")));
        isFlowOptimized = false;

        calculateNumericallyStableNewRiskThreshold(action);

        var stateReward = innerApplyAction(action);
        logger.debug("New Global risk: [{}]", totalRiskAllowed);
        return stateReward;
    }

    @Override
    public boolean updateTree() {
        isFlowOptimized = false;
        return super.updateTree();
    }

    private double getPlayerActionProbability(TAction appliedAction) {
        return calculateNumericallyStableActionProbability(getRoot()
            .getChildNodeMap()
            .get(appliedAction)
            .getSearchNodeMetadata()
            .getNodeProbabilityFlow()
            .getSolution());
    }

    private double getOpponentActionProbability(TAction appliedAction) {
        return getRoot()
            .getChildNodeMap()
            .get(appliedAction)
            .getSearchNodeMetadata()
            .getPriorProbability();
    }

    private void calculateNumericallyStableNewRiskThreshold(TAction appliedAction) {

        if(getRoot().getChildNodeMap().get(appliedAction).isFinalNode()) {
            totalRiskAllowed = 1.0; // CORRECT?
            return;
        }

        // TODO: pokud exploruji, tak brat pravdepodobnost z distribuce, kterou epxloruji a ne z linearni optimalizace

        if(appliedAction.isPlayerAction()) {
            parentPathProbability = 0.0;
            acumulatedRiskOfOtherActions = 0.0;
        }
        double riskOfOtherActions = calculateNumericallyStableRiskOfAnotherActions(appliedAction);
        if(getRoot().getChildNodeMap().get(appliedAction).isPlayerTurn()) {
            if(getRoot().isPlayerTurn()) {
                //TODO: why the fuck do I allow opponent to play multiple actions but player can play only one in a row?
                throw new IllegalStateException("Player can't play two actions in a row");
            }
            double totalOtherActionsRiskSum = acumulatedRiskOfOtherActions + riskOfOtherActions;
            double riskDiff = calculateNumericallyStableRiskDiff(totalOtherActionsRiskSum);
            totalRiskAllowed = calculateNewRiskValue(
                riskDiff,
                // parentPathProbability * getOpponentActionProbability(appliedAction),
//                parentPathProbability *
                    calculateNumericallyStableActionProbability(getRoot()
                        .getChildNodeMap()
                        .get(appliedAction)
                        .getSearchNodeMetadata()
                        .getNodeProbabilityFlow()
                        .getSolution()),
                totalOtherActionsRiskSum,
                appliedAction);
        } else {
            if(appliedAction.isPlayerAction()) {
                acumulatedRiskOfOtherActions += riskOfOtherActions;
                parentPathProbability = getPlayerActionProbability(appliedAction);
            } else {
                acumulatedRiskOfOtherActions += riskOfOtherActions * parentPathProbability;
                parentPathProbability *= getOpponentActionProbability(appliedAction);
            }
        }
    }

    private double calculateNumericallyStableRiskOfAnotherActions(TAction appliedAction) {
        double riskOfOtherActions = 0.0;
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : getRoot().getChildNodeMap().entrySet()) {
            if(entry.getKey() != appliedAction) {
                double risk = calculateRiskContributionInSubTree(entry.getValue());
//                double risk = calculateRiskContributionPerOneAction(entry.getValue(), getPlayerActionProbability(entry.getKey()));
//                double risk = calculateRiskContributionInSubTree(entry.getValue()) * (getRoot().isPlayerTurn() ? getPlayerActionProbability(entry.getKey()) : 1.0);
//                double risk = calculateRiskContributionInSubTree(entry.getValue()) * calculateNumericallyStableActionProbability(getRoot()
//                    .getChildNodeMap()
//                    .get(appliedAction)
//                    .getSearchNodeMetadata()
//                    .getNodeProbabilityFlow()
//                    .getSolution());

                logger.debug("Risk for [{}] action-subtree is [{}]", entry.getKey(), risk);
                riskOfOtherActions += risk;
            }
        }

        if(Math.abs(riskOfOtherActions) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            if (riskOfOtherActions != 0.0) {
                logger.debug("Rounding risk of other actions to 0. This is done because linear optimization is not numerically stable");
            }
            return 0.0;
        } else if(Math.abs(riskOfOtherActions - 1.0) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            if(riskOfOtherActions != 1.0) {
                logger.debug("Rounding risk of other actions to 1. This is done because linear optimization is not numerically stable");
            }
            return 1.0;
        } else if(riskOfOtherActions < 0.0) {
            throw new IllegalStateException("Risk of other actions cannot be lower than 0. This would cause program failure later in simulation");
        } else if(riskOfOtherActions > 1.0) {
            throw new IllegalStateException("Risk of other actions cannot be higher than 1. This would cause program failure later in simulation");
        }
        return riskOfOtherActions;

    }

    private double calculateNewRiskValue(double riskDiff, double actionProbability, double riskOfOtherActions, TAction appliedAction) {
        if(actionProbability == 0.0) {
            logger.debug("Taken action with zero probability according to linear optimization. Setting risk to 1.0, since such action is probably taken due to exploration.");
            return 1.0;
        }
        if(Math.abs(totalRiskAllowed - 1.0) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            logger.warn("Risk is set to 1 already, no recalculation is needed");
            return 1.0;
        }
        double newRisk = riskDiff / actionProbability;
        if((newRisk < -NUMERICAL_RISK_DIFF_TOLERANCE) || (newRisk - 1.0 > NUMERICAL_RISK_DIFF_TOLERANCE)) {
//            this.printTreeToFile(this.latestTreeWithPlayerOnTurn, "TreeDump_player", 100);
//            this.printTreeToFile(this.getRoot(), "TreeDump_latest", 100);

            this.printTreeToFileWithFlowNodesOnly(this.latestTreeWithPlayerOnTurn, "TreeDump_player");
            this.printTreeToFileWithFlowNodesOnly(this.getRoot(), "TreeDump_latest");
            throw new IllegalStateException(
                "Risk out of bounds. " +
                    "Old risk [" + totalRiskAllowed + "]. " +
                    "Risk diff numerically stabilised: [" +  riskDiff + "] " +
                    "New risk calculated: [" + newRisk + "], " +
                    "Numerically stable risk of other actions: [" + riskOfOtherActions + "], " +
                    "Dividing probability: [" + getRoot().getChildNodeMap().get(appliedAction).getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() + "], " +
                    "Numerically stabilised dividing probability: [" + actionProbability + "]" +
                    "In environment: " + System.lineSeparator() +
                    "" + getRoot().getWrappedState().readableStringRepresentation());
        }
        if(newRisk > 1.0) {
            logger.debug("Rounding new risk from [{}] to 1.0.", newRisk);
            return 1.0;
        }
        if(newRisk < 0.0) {
            logger.debug("Rounding newRisk from [{}] to 0.0", newRisk);
            return 0.0;
        }
        return newRisk;

    }

    private double calculateNumericallyStableActionProbability(double calculatedProbability) {
        if(Math.abs(calculatedProbability) < NUMERICAL_PROBABILITY_TOLERANCE) {
            if (calculatedProbability != 0.0) {
                logger.debug("Rounding action probability from [{}] to 0. This is done because linear optimization is not numerically stable", calculatedProbability);
            }
            return 0.0;
        } else if(Math.abs(calculatedProbability - 1.0) < NUMERICAL_PROBABILITY_TOLERANCE) {
            if(calculatedProbability != 1.0) {
                logger.debug("Rounding action probability from [{}] to 1. This is done because linear optimization is not numerically stable", calculatedProbability);
            }
            return 1.0;
        } else if(calculatedProbability < 0.0) {
            throw new IllegalStateException("Probability cannot be lower than 0. Actual value: [" + calculatedProbability + "]. This would cause program failure later in simulation");
        } else if(calculatedProbability > 1.0) {
            throw new IllegalStateException("Probability cannot be higher than 1. Actual value: [" + calculatedProbability + "]. This would cause program failure later in simulation");
        }
        return calculatedProbability;
    }

    private double calculateNumericallyStableRiskDiff(double totalRiskOfOtherActions) {
        double riskDiff = (totalRiskAllowed - totalRiskOfOtherActions);
        if(Math.abs(riskDiff) < NUMERICAL_RISK_DIFF_TOLERANCE) {
            if(riskDiff != 0) {
                logger.debug("Rounding risk difference to 0. This si done because linear optimization is not numerically stable");
            }
            riskDiff = 0.0;
        } else if(riskDiff < 0.0) {
            throw new IllegalStateException("Risk difference is out of bounds. New risk difference [" + riskDiff + "]. Risk exceeds tolerated bound: [" + -NUMERICAL_RISK_DIFF_TOLERANCE + "]");
        }
        return riskDiff;
    }

//    private double calculateRiskContributionPerOneAction(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node, double nodeProbability) {
//        return calculateRiskContributionInSubTree(node) * (node.isPlayerTurn() ? 1.0 :  nodeProbability);
//
////        if(node.isPlayerTurn()) {
////            return calculateRiskContributionInSubTree(node) * nodeProbability;
////        } else {
////            return node
////                .getChildNodeStream()
////                .mapToDouble(x -> calculateRiskContributionPerOneAction(x, x.getSearchNodeMetadata().getPriorProbability()))
////                .sum() * nodeProbability;
////        }
//    }

    private double calculateRiskContributionInSubTree(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;

        LinkedList<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> queue = new LinkedList<>();
        queue.addFirst(subTreeRoot);

        while(!queue.isEmpty()) {
            SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node = queue.poll();
            if(node.isLeaf()) {
                if(node.isFinalNode()) {
                    risk += node.getWrappedState().isRiskHit() ? node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() : 0.0;
//                    risk += node.getWrappedState().isRiskHit() ? 1.0 : 0.0;
                } else {
//                    risk += node.getSearchNodeMetadata().getPredictedRisk() * node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();

                }
            } else {
                for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

    private void printTreeToFileWithFlowNodesOnly(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName) {
        printTreeToFileInternal(subtreeRoot, fileName, Integer.MAX_VALUE, a -> a.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() != 0);
    }

}
