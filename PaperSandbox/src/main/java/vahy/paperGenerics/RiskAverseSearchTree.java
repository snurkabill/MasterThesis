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
import vahy.environment.state.PaperState;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.tree.SearchTreeImpl;

import java.util.LinkedList;
import java.util.Map;

public class RiskAverseSearchTree<
    TAction extends Action,
    TReward extends DoubleReward,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>> extends SearchTreeImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(RiskAverseSearchTree.class);

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -10);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -10);
    public static final double NUMERICAL_ACTION_RISK_TOLERANCE = Math.pow(10, -10);

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    public RiskAverseSearchTree(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> root,
                                NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                double totalRiskAllowed) {
        super(root, nodeSelector, treeUpdater, nodeEvaluator);
        this.totalRiskAllowed = totalRiskAllowed;
    }

    public double getTotalRiskAllowed() {
        return totalRiskAllowed;
    }

    public boolean isFlowOptimized() {
        return isFlowOptimized;
    }

    public void setFlowOptimized(boolean flowOptimized) {
        isFlowOptimized = flowOptimized;
    }

    @Override
    public StateRewardReturn<TAction, TReward, TObservation, TState> applyAction(TAction action) {
        if(!getRoot().getChildNodeMap().containsKey(action)) {
            throw new IllegalStateException("Action [" + action + "] is invalid and cannot be applied to current policy state");
        }
        isFlowOptimized = false;
        if(action.isPlayerAction()) {
            calculateNumericallyStableNewRiskThreshold(action);
        }
        return super.applyAction(action);
    }

    @Override
    public boolean updateTree() {
        isFlowOptimized = false;
        return super.updateTree();
    }

    private void calculateNumericallyStableNewRiskThreshold(TAction appliedAction) {
        double riskOfOtherActions = calculateNumericallyStableRiskOfAnotherActions(appliedAction);
        double riskDiff = calculateNumericallyStableRiskDiff(riskOfOtherActions);
        double actionProbability = calculateNumericallyStableActionProbability(getRoot()
            .getChildNodeMap()
            .get(appliedAction)
            .getSearchNodeMetadata()
            .getNodeProbabilityFlow()
            .getSolution());
        totalRiskAllowed = calculateNewRiskValue(riskDiff, actionProbability, riskOfOtherActions, appliedAction);
    }

    private double calculateNumericallyStableRiskOfAnotherActions(TAction appliedAction) {
        double riskOfOtherActions = 0.0;
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> entry : getRoot().getChildNodeMap().entrySet()) {
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

    private double calculateNewRiskValue(double riskDiff, double actionProbability, double riskOfOtherActions, TAction appliedAction) {
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
                        "Dividing probability: [" + getRoot().getChildNodeMap().get(appliedAction).getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() + "], " +
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

    private double calculateRiskContributionInSubTree(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;

        LinkedList<SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> queue = new LinkedList<>();
        queue.addFirst(subTreeRoot);

        while(!queue.isEmpty()) {
            SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node = queue.poll();
            if(node.isLeaf()) {
                if(node.getWrappedState().isRiskHit()) {
                    risk += node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();
                }
            } else {
                for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

}
