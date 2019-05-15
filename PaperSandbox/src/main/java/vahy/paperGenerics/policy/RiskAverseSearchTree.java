package vahy.paperGenerics.policy;

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
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PolicyMode;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistribution;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
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
    public static final double ZERO_TEMPERATURE = 0.0;

    private final SplittableRandom random;

    private SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> latestTreeWithPlayerOnTurn = null;

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    private final List<TAction> playerActions;

    private PlayingDistribution<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playingDistribution;

    private final StrategiesProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider;

    public RiskAverseSearchTree(Class<TAction> clazz,
                                SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root,
                                NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                SplittableRandom random,
                                double totalRiskAllowed,
                                StrategiesProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategyProvider) {
        super(root, nodeSelector, treeUpdater, nodeEvaluator);
        TAction[] allActions = clazz.getEnumConstants();
        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).collect(Collectors.toCollection(ArrayList::new));
        this.random = random;
        this.totalRiskAllowed = totalRiskAllowed;
        this.strategiesProvider = strategyProvider;
    }

    private PlayingDistribution<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> inferencePolicyBranch(TState state) {
        if(tryOptimizeFlow()) {
            return strategiesProvider.provideInferenceExistingFlowStrategy(state, playerActions, totalRiskAllowed, ZERO_TEMPERATURE).createDistribution(getRoot());
        } else {
            return strategiesProvider.provideInferenceNonExistingFlowStrategy(state, playerActions, totalRiskAllowed, ZERO_TEMPERATURE).createDistribution(getRoot());
        }
    }

    private PlayingDistribution<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> explorationPolicyBranch(TState state, double temperature) {
        if(tryOptimizeFlow()) {
            return strategiesProvider.provideExplorationExistingFlowStrategy(state, playerActions, totalRiskAllowed, temperature).createDistribution(getRoot());
        } else {
            return strategiesProvider.provideExplorationNonExistingFlowStrategy(state, playerActions, totalRiskAllowed, temperature).createDistribution(getRoot());
        }
    }

    private PlayingDistribution<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createActionWithDistribution(TState state,
                                                                                                                                                      PolicyMode policyMode,
                                                                                                                                                      double temperature) {
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
        try {
            this.playingDistribution = createActionWithDistribution(state, policyMode, temperature);
        return new ImmutableTuple<>(playingDistribution.getExpectedPlayerAction(), playingDistribution.getPlayerDistribution());
        } catch(Exception e) {
            dumpTree();
            throw e;
        }
    }

    private void dumpTree() {
        if(this.latestTreeWithPlayerOnTurn != null) {
            this.printTreeToFileWithFlowNodesOnly(this.latestTreeWithPlayerOnTurn, "TreeDump_player");
        }
        this.printTreeToFileWithFlowNodesOnly(this.getRoot(), "TreeDump_latest");
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
            var result = strategiesProvider.provideFlowOptimizer().optimizeFlow(getRoot(), random, totalRiskAllowed);
            totalRiskAllowed = result.getFirst();
            if(!result.getSecond()) {
                logger.error("Solution to flow optimisation does not exist. Setting allowed risk to 1.0 in state: [" + getRoot().getWrappedState().readableStringRepresentation() + "] with allowed risk: [" + totalRiskAllowed + "]");
                totalRiskAllowed = 1.0;
                isFlowOptimized = false;
                return false;
            }
            isFlowOptimized = true;
            return true;
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
//            throw new IllegalStateException("Risk [" + riskName + "] cannot be negative. Actual value: [" + risk + "]");
            logger.warn("Risk [" + riskName + "] cannot be negative. Actual value: [" + risk + "]");
            return 0.0;
        } else
            if(risk < 0.0) {
            logger.debug("Rounding risk [{}] with value [{}] to 0.0", riskName, risk);
            return 0.0;
        } else {
            return risk;
        }
    }

    @Override
    public StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
        try {
            if(action.isPlayerAction() && action != playingDistribution.getExpectedPlayerAction()) {
                throw new IllegalStateException("RiskAverseTree is applied with player action which was not selected by riskAverseTree. Discrepancy.");
            }
            if(action.isPlayerAction()) { // debug purposes
                latestTreeWithPlayerOnTurn = this.getRoot();
            }
            isFlowOptimized = false;
            if(!action.isPlayerAction() && !isRiskIgnored()) {
                var playerActionDistribution = playingDistribution.getPlayerDistribution();
                var riskEstimatedVector = playingDistribution.getRiskOnPlayerSubNodes();
                var playerActionProbability = playerActionDistribution[playingDistribution.getExpectedPlayerActionIndex()];
                var opponentActionProbability = getRoot().getSearchNodeMetadata().getChildPriorProbabilities().get(action);
                var riskOfOtherPlayerActions = 0.0d;
                for (int i = 0; i < riskEstimatedVector.length; i++) {
                    if(i != playingDistribution.getExpectedPlayerActionIndex()) {
                        riskOfOtherPlayerActions += riskEstimatedVector[i] * playerActionDistribution[i];
                    }
                }
                riskOfOtherPlayerActions = roundRiskIfBelowZero(riskOfOtherPlayerActions, "RiskOfOtherPlayerActions");
                var riskOfOtherOpponentActions = getRoot()
                    .getChildNodeStream()
                    .filter(x -> x.getAppliedAction() != action)
                    .mapToDouble(x ->
                        playingDistribution.getUsedSubTreeRiskCalculatorSupplier().get().calculateRisk(x) *
                        x.getSearchNodeMetadata().getPriorProbability() *
                        playerActionProbability
                    )
                    .sum();
                riskOfOtherOpponentActions = roundRiskIfBelowZero(riskOfOtherOpponentActions, "RiskOfOtherOpponentActions");

                var dividingProbability = (playerActionProbability * opponentActionProbability);

                var oldRisk = totalRiskAllowed;

//                if(Arrays.stream(riskEstimatedVector).sum() != 0.0) {
//                    totalRiskAllowed = (totalRiskAllowed - (riskOfOtherPlayerActions + riskOfOtherOpponentActions)) / dividingProbability;
//                    totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
//                }

                if(Arrays.stream(riskEstimatedVector).anyMatch(value -> value > 0.0)) {
                    totalRiskAllowed = (totalRiskAllowed - (riskOfOtherPlayerActions + riskOfOtherOpponentActions)) / dividingProbability;
                    totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
                }

//                logger.info("Global risk: [{}]", totalRiskAllowed);

                logger.debug("Playing action: [{}] from actions: [{}]) with distribution: [{}] with minimalRiskReachAbility: [{}]. Risk of other player actions: [{}]. Risk of other Opponent actions: [{}], dividing probability: [{}], old risk: [{}], new risk: [{}]",
                    playingDistribution.getExpectedPlayerAction(),
                    playerActions.stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElseThrow(() -> new IllegalStateException("Result of reduce does not exist")),
                    Arrays.toString(playerActionDistribution),
                    Arrays.toString(riskEstimatedVector),
                    riskOfOtherPlayerActions,
                    riskOfOtherOpponentActions,
                    dividingProbability,
                    oldRisk,
                    totalRiskAllowed
                    );


                if(totalRiskAllowed > 1.0 + NUMERICAL_RISK_DIFF_TOLERANCE) {
                    logger.warn("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                    totalRiskAllowed = 1.0;
//                    throw new IllegalStateException("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                }

                logger.debug("New Global risk: [{}]", totalRiskAllowed);
            }
            return innerApplyAction(action);

        } catch(Exception e) {
            dumpTree();
            throw e;
        }
    }


    private void printTreeToFileWithFlowNodesOnly(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName) {
        printTreeToFileInternal(subtreeRoot, fileName, Integer.MAX_VALUE, a -> a.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() != 0);
    }

}
