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
import vahy.paperGenerics.policy.linearProgram.OptimalFlowHardConstraintCalculator;
import vahy.paperGenerics.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTriple;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private final SplittableRandom random;

    private SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> latestTreeWithPlayerOnTurn = null;

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    private double playedPlayerProbability = 0.0;
    private double playerRiskOfOtherActions = 0.0;

    private TAction expectedPlayerAction;
    private int indexOfExpectedPlayerAction;
    private double[] playerActionDistribution;
    private double[] playerRiskEstimatedDistribution;
    private final List<TAction> playerActions;

    private final Supplier<SubtreeRiskCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier;

    public RiskAverseSearchTree(Class<TAction> clazz,
                                SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root,
                                NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                SplittableRandom random,
                                double totalRiskAllowed) {
        super(root, nodeSelector, treeUpdater, nodeEvaluator);
        TAction[] allActions = clazz.getEnumConstants();
        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).collect(Collectors.toCollection(ArrayList::new));
        this.random = random;
        this.totalRiskAllowed = totalRiskAllowed;
        this.subtreeRiskCalculatorSupplier = () -> new MinimalRiskReachAbilityCalculator<>(random);
//        this.subtreeRiskCalculatorSupplier = SubtreePriorRiskCalculator::new;
    }

    private ImmutableTriple<List<TAction>, double[], double[]> getUcbVisitDistribution() {
        double totalVisitSum = getRoot()
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getVisitCounter())
            .sum();
        return createDistributionAsArray(getRoot()
            .getChildNodeStream()
            .map(x -> new ImmutableTriple<>(x.getAppliedAction(), x.getSearchNodeMetadata().getVisitCounter() / totalVisitSum, 1.0d))
            .collect(Collectors.toList()));
    }

    private ImmutableTriple<List<TAction>, double[], double[]> createDistributionAsArray(List<ImmutableTriple<TAction, Double, Double>> actionDistribution) {
        var actionVector = new double[playerActions.size()];
        var riskVector = new double[playerActions.size()];
        var actionList = new ArrayList<TAction>(playerActions.size());
        for (ImmutableTriple<TAction, Double, Double> entry : actionDistribution) {
            int actionIndex = entry.getFirst().getActionIndexInPossibleActions();
            actionList.add(actionIndex, entry.getFirst());
            actionVector[actionIndex] = entry.getSecond();
            riskVector[actionIndex] = entry.getThird();
        }
        return new ImmutableTriple<>(actionList, actionVector, riskVector);
    }

    private ImmutableTriple<ImmutableTuple<TAction, Integer>, double[], double[]> inferencePolicyBranch(TState state) {
        if(tryOptimizeFlow()) {
            var alternateDistribution = createDistributionAsArray(getRoot()
                    .getChildNodeStream()
                    .map(x -> {
                        var probabilityFlowFromGlobalOptimization = x.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();
                        var minimalRiskReachAbilityCalculator = subtreeRiskCalculatorSupplier.get();
                        var subtreeRisk = minimalRiskReachAbilityCalculator.calculateRisk(x);
                        return new ImmutableTriple<>(x.getAppliedAction(), probabilityFlowFromGlobalOptimization, subtreeRisk);
                    })
                    .collect(Collectors.toList()));
            RandomDistributionUtils.tryToRoundDistribution(alternateDistribution.getSecond());
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(alternateDistribution.getSecond(), random);
            return new ImmutableTriple<>(new ImmutableTuple<>(alternateDistribution.getFirst().get(index), index), alternateDistribution.getSecond(), alternateDistribution.getThird());
        } else {
            var ucbDistribution = getUcbVisitDistribution();
            var max = ucbDistribution.getSecond()[0];
            var index = 0;
            for (int i = 1; i < ucbDistribution.getFirst().size(); i++) {
                if(max < ucbDistribution.getSecond()[i]) {
                    max = ucbDistribution.getSecond()[i];
                    index = i;
                }
            }
            return new ImmutableTriple<>(new ImmutableTuple<>(ucbDistribution.getFirst().get(index), index), ucbDistribution.getSecond(), ucbDistribution.getThird());
        }
    }


    private ImmutableTriple<ImmutableTuple<TAction, Integer>, double[], double[]> explorationPolicyBranch(TState state, double temperature) {
        if(tryOptimizeFlow()) {
            var alternateDistribution = createDistributionAsArray(getRoot()
                .getChildNodeStream()
                .map(x -> {
                    var probabilityFlowFromGlobalOptimization = x.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();
                    var minimalRiskReachAbilityCalculator = subtreeRiskCalculatorSupplier.get();
                    var subtreeRisk = minimalRiskReachAbilityCalculator.calculateRisk(x);
                    return new ImmutableTriple<>(x.getAppliedAction(), probabilityFlowFromGlobalOptimization, subtreeRisk);
                })
                .collect(Collectors.toList()));

            double[] actionDistributionAsArray = alternateDistribution.getSecond();
            RandomDistributionUtils.tryToRoundDistribution(actionDistributionAsArray);
            RandomDistributionUtils.applyBoltzmannNoise(actionDistributionAsArray, temperature);
            double[] actionRiskAsArray = alternateDistribution.getThird();

            var sum = 0.0d;
            for (int i = 0; i < actionDistributionAsArray.length; i++) {
                sum += actionDistributionAsArray[i] * actionRiskAsArray[i];
            }
            if(sum > totalRiskAllowed) {
                var suitableExplorationDistribution = RandomDistributionUtils.findSimilarSuitableDistributionByLeastSquares(
                    actionDistributionAsArray,
                    alternateDistribution.getThird(),
                    totalRiskAllowed);
                int index = RandomDistributionUtils.getRandomIndexFromDistribution(suitableExplorationDistribution, random);
                return new ImmutableTriple<>(new ImmutableTuple<>(alternateDistribution.getFirst().get(index), index), suitableExplorationDistribution, alternateDistribution.getThird());
            } else {
                int index = RandomDistributionUtils.getRandomIndexFromDistribution(actionDistributionAsArray, random);
                return new ImmutableTriple<>(new ImmutableTuple<>(alternateDistribution.getFirst().get(index), index), actionDistributionAsArray, alternateDistribution.getThird());
            }
        } else {
            var ucbDistribution = getUcbVisitDistribution();
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(ucbDistribution.getSecond(), random);
            return new ImmutableTriple<>(new ImmutableTuple<>(ucbDistribution.getFirst().get(index), index), ucbDistribution.getSecond(), ucbDistribution.getThird());
        }
    }

    private ImmutableTriple<ImmutableTuple<TAction, Integer>, double[], double[]> createActionWithDistribution(TState state, PolicyMode policyMode, double temperature) {
        switch (policyMode) {
            case EXPLOITATION:
                return inferencePolicyBranch(state);
            case EXPLORATION:
                return explorationPolicyBranch(state, temperature);
            default: throw EnumUtils.createExceptionForUnknownEnumValue(policyMode);
        }
    }

    public ImmutableTuple<TAction, double[]> getActionDistributionAndDiscreteAction(TState state, PolicyMode policyMode, double temperature) {
        if(state.isOpponentTurn()) {
            throw new IllegalStateException("Cannot determine action distribution on opponent's turn");
        }
        var actionWithActionDistribution = createActionWithDistribution(state, policyMode, temperature);

        logger.debug("Playing action: [{}] from actions: [{}]) with distribution: [{}] with minimalRiskReachAbility: [{}]",
            playerActions.get(actionWithActionDistribution.getFirst().getSecond()),
            playerActions.stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElseThrow(() -> new IllegalStateException("Result of reduce does not exist")),
            Arrays.toString(actionWithActionDistribution.getSecond()),
            Arrays.toString(actionWithActionDistribution.getThird()));

        expectedPlayerAction = actionWithActionDistribution.getFirst().getFirst();
        indexOfExpectedPlayerAction = actionWithActionDistribution.getFirst().getSecond();
        playerActionDistribution = actionWithActionDistribution.getSecond();
        playerRiskEstimatedDistribution = actionWithActionDistribution.getThird();

        return new ImmutableTuple<>(actionWithActionDistribution.getFirst().getFirst(), actionWithActionDistribution.getSecond());
    }

    public boolean isFlowOptimized() {
        return isFlowOptimized;
    }

    public boolean isRiskIgnored() {
        return totalRiskAllowed >= 1.0;
    }

    private boolean tryOptimizeFlow() {
        if(isRiskIgnored()) {
            isFlowOptimized = false;
            return false;
        }
        if(!isFlowOptimized) {
            var optimalFlowCalculator = new OptimalFlowHardConstraintCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>(random, totalRiskAllowed);
            boolean optimalSolutionExists = optimalFlowCalculator.optimizeFlow(getRoot());
            if(!optimalSolutionExists) {
                logger.error("Solution to flow optimisation does not exist. Setting allowed risk to 1.0 in state: [" + getRoot().getWrappedState().readableStringRepresentation() + "] with allowed risk: [" + totalRiskAllowed + "]");
                totalRiskAllowed = 1.0;
                isFlowOptimized = false;
                return false;
            } else {
                isFlowOptimized = true;
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean updateTree() {
        isFlowOptimized = false;
        return super.updateTree();
    }

    private double roundRiskIfBelowZero(double risk, String riskName) {
        if(risk < 0.0 - NUMERICAL_RISK_DIFF_TOLERANCE) {
            throw new IllegalStateException("Risk [" + riskName + "] cannot be negative. Actual value: [" + risk + "]");
        } else if(risk < 0.0) {
            logger.debug("Rounding risk [{}] with value [{}] to 0.0", riskName, risk);
            return 0.0;
        } else {
            return risk;
        }
    }

    @Override
    public StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
        try {
            if(action.isPlayerAction() && action != expectedPlayerAction) {
                throw new IllegalStateException("RiskAverseTree is applied with player action which was not selected by riskAverseTree. Discrepancy.");
            }
            if(action.isPlayerAction()) { // debug purposes
                latestTreeWithPlayerOnTurn = this.getRoot();
            }
            isFlowOptimized = false;
            if(!action.isPlayerAction()) {
                var playerActionProbability = playerActionDistribution[indexOfExpectedPlayerAction];
                var opponentActionProbability = getRoot().getSearchNodeMetadata().getChildPriorProbabilities().get(action);
                var riskOfOtherPlayerActions = 0.0d;
                for (int i = 0; i < playerRiskEstimatedDistribution.length; i++) {
                    if(i != indexOfExpectedPlayerAction) {
                        riskOfOtherPlayerActions += playerRiskEstimatedDistribution[i] * playerActionDistribution[i];
                    }
                }
                riskOfOtherPlayerActions = roundRiskIfBelowZero(riskOfOtherPlayerActions, "RiskOfOtherPlayerActions");
                var riskOfOtherOpponentActions = getRoot()
                    .getChildNodeStream()
                    .filter(x -> x.getAppliedAction() != action)
                    .mapToDouble(x -> {
                        var minimalRiskReachAbilityCalculator = subtreeRiskCalculatorSupplier.get();
                        return minimalRiskReachAbilityCalculator.calculateRisk(x) * x.getSearchNodeMetadata().getPriorProbability() * playerActionProbability;
                    })
                    .sum();
                riskOfOtherOpponentActions = roundRiskIfBelowZero(riskOfOtherOpponentActions, "RiskOfOtherOpponentActions");

                var dividingProbability = (playerActionProbability * opponentActionProbability);

                var oldRisk = totalRiskAllowed;

                if(Arrays.stream(playerRiskEstimatedDistribution).sum() != 0.0) {
                    totalRiskAllowed = (totalRiskAllowed - (riskOfOtherPlayerActions + riskOfOtherOpponentActions)) / dividingProbability;
                    totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
                }

                logger.debug("Playing action: [{}] from actions: [{}]) with distribution: [{}] with minimalRiskReachAbility: [{}]. Risk of other player actions: [{}]. Risk of other Opponent actions: [{}], dividing probability: [{}], old risk: [{}], new risk: [{}]",
                    expectedPlayerAction,
                    playerActions.stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElseThrow(() -> new IllegalStateException("Result of reduce does not exist")),
                    Arrays.toString(playerActionDistribution),
                    Arrays.toString(playerRiskEstimatedDistribution),
                    riskOfOtherPlayerActions,
                    riskOfOtherOpponentActions,
                    dividingProbability,
                    oldRisk,
                    totalRiskAllowed
                    );


                if(totalRiskAllowed > 1.0 + NUMERICAL_RISK_DIFF_TOLERANCE) {
                    logger.error("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                    totalRiskAllowed = 1.0;
//                    throw new IllegalStateException("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                }

                logger.debug("New Global risk: [{}]", totalRiskAllowed);
            }
            return innerApplyAction(action);


//
//            if(action.isPlayerAction()) {
//
//            } else {
//                checkApplicableAction(action);
//                if(action.isPlayerAction()) { // debug purposes
//                    latestTreeWithPlayerOnTurn = this.getRoot();
//                }
//                logger.debug("Old Global risk: [{}] and applying action: [{}] with probability: [{}]", totalRiskAllowed, action, action.isPlayerAction()
//                    ? getPlayerActionProbability(action)
//                    : getOpponentActionProbability(action));
//                logger.debug("Action probability distribution: [{}]", getRoot()
//                    .getChildNodeStream()
//                    .map(x -> action.isPlayerAction() ? getPlayerActionProbability(x.getAppliedAction()) : getOpponentActionProbability(x.getAppliedAction()))
//                    .map(Object::toString)
//                    .reduce((s, s2) -> s + ", " + s2)
//                    .orElseThrow(() -> new IllegalStateException("Reduce op does not exists")));
//
//
//
//                if(!isRiskIgnored()) {
//                    calculateNumericallyStableNewRiskThreshold(action);
//                }
//                isFlowOptimized = false;
//                var stateReward = innerApplyAction(action);
//                logger.debug("New Global risk: [{}]", totalRiskAllowed);
//                return stateReward;
//            }
//
//
//

        } catch(Exception e) {
            this.printTreeToFileWithFlowNodesOnly(this.latestTreeWithPlayerOnTurn, "TreeDump_player");
            this.printTreeToFileWithFlowNodesOnly(this.getRoot(), "TreeDump_latest");
            throw e;
        }
    }

//    private List<TAction> getAllowedActionsForExploration() {
//        TAction[] actions = getRoot().getAllPossibleActions();
//        var allowedActions = new LinkedList<TAction>();
//        for (TAction action : actions) {
//            if (calculateRiskOfOpponentNodes(getRoot().getChildNodeMap().get(action)) <= totalRiskAllowed) {
//                allowedActions.add(action);
//            }
//        }
//        return allowedActions;
//    }
//
//    private double calculateRiskOfOpponentNodes(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
//        if(node.isFinalNode()) {
//            return node.getWrappedState().isRiskHit() ?  1.0 : 0.0;
//        }
//        if(node.isPlayerTurn()) {
//            return 0.0;
//        }
//        if(node.isLeaf()) {
//            throw new IllegalStateException("Risk can't be calculated from leaf nodes which are not player turns. Tree should be expanded up to player or final nodes");
//        }
//        return node
//            .getChildNodeStream()
//            .map(x -> new ImmutableTuple<>(x, x.getSearchNodeMetadata().getPriorProbability()))
//            .mapToDouble(x -> calculateRiskOfOpponentNodes(x.getFirst()) * x.getSecond())
//            .sum();
//    }

    private double getPlayerActionProbability(TAction appliedAction) {
        if(isFlowOptimized) {
            return calculateNumericallyStableActionProbability(getRoot()
                .getChildNodeMap()
                .get(appliedAction)
                .getSearchNodeMetadata()
                .getNodeProbabilityFlow()
                .getSolution());
        } else {
            double sum = getRoot().getChildNodeStream().mapToDouble(x -> x.getSearchNodeMetadata().getVisitCounter()).sum();
            return getRoot().getChildNodeMap().get(appliedAction).getSearchNodeMetadata().getVisitCounter() / sum;
        }
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
        if(appliedAction.isPlayerAction()) {
            playedPlayerProbability = 0.0;
            playerRiskOfOtherActions = 0.0;
        }
        double riskOfOtherActions = calculateNumericallyStableRiskOfAnotherActions(appliedAction);
        if(getRoot().getChildNodeMap().get(appliedAction).isPlayerTurn()) {
            if(getRoot().isPlayerTurn()) {
                //TODO: why the fuck do I allow opponent to play multiple actions but player can play only one in a row?
                throw new IllegalStateException("Player can't play two actions in a row");
            }
            double totalOtherActionsRiskSum = playerRiskOfOtherActions + riskOfOtherActions;
            double riskDiff = calculateNumericallyStableRiskDiff(totalOtherActionsRiskSum);
            totalRiskAllowed = calculateNewRiskValue(
                riskDiff,
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
                playerRiskOfOtherActions = riskOfOtherActions;
                playedPlayerProbability = getPlayerActionProbability(appliedAction);
            } else {
                throw new IllegalStateException("can't apply another opponent's action");
            }
        }
    }

    private double calculateNumericallyStableRiskOfAnotherActions(TAction appliedAction) {
        double riskOfOtherActions = 0.0;
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : getRoot().getChildNodeMap().entrySet()) {
            if(entry.getKey() != appliedAction) {
                double risk = calculateRiskContributionInSubTree(entry.getValue());
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
            throw new IllegalStateException("Risk of other actions cannot be lower than 0. Actual value: [ " + riskOfOtherActions + " ]. This would cause program failure later in simulation");
        } else if(riskOfOtherActions > 1.0) {
            throw new IllegalStateException("Risk of other actions cannot be higher than 1. Actual value: [ " + riskOfOtherActions + " ]. This would cause program failure later in simulation");
        }
        return riskOfOtherActions;

    }

    private double calculateNewRiskValue(double riskDiff, double actionProbability, double riskOfOtherActions, TAction appliedAction) {
        if(actionProbability == 0.0) {
            logger.debug("Taken action with zero probability according to linear optimization. Setting risk to 1.0, since such action is probably taken due to exploration.");
            return 1.0;
        }
        if(Math.abs(totalRiskAllowed - 1.0) < NUMERICAL_ACTION_RISK_TOLERANCE) {
            logger.debug("Risk is set to 1 already, no recalculation is needed");
            return 1.0;
        }
        double newRisk = riskDiff / actionProbability;
        if((newRisk < -NUMERICAL_RISK_DIFF_TOLERANCE) || (newRisk - 1.0 > NUMERICAL_RISK_DIFF_TOLERANCE)) {
//            this.printTreeToFile(this.latestTreeWithPlayerOnTurn, "TreeDump_player", 100);
//            this.printTreeToFile(this.getRoot(), "TreeDump_latest", 100);
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
                    risk += node.getSearchNodeMetadata().getPredictedRisk() * node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();

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
