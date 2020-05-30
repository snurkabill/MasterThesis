package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.paperGenerics.PaperState;
import vahy.api.policy.PolicyStepMode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.flowOptimizer.AbstractFlowOptimizer;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistribution;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.SplittableRandom;

public class RiskAverseSearchTree<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends SearchTreeImpl<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(RiskAverseSearchTree.class);
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_ACTION_RISK_TOLERANCE = Math.pow(10, -13);
    public static final double INVALID_TEMPERATURE_VALUE = -Double.MAX_VALUE;

    private final SplittableRandom random;

    private SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> latestTreeWithPlayerOnTurn = null;

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    private PlayingDistribution<TAction, TObservation, TSearchNodeMetadata, TState> playingDistribution;
    private SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRiskCalculator;

    private final RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector;

    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> inferenceExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> inferenceNonExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> explorationExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> explorationNonExistingFlowDistribution;
    private final AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata, TState> flowOptimizer;


    public RiskAverseSearchTree(int policyId,
                                SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root,
                                RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                SplittableRandom random,
                                double totalRiskAllowed,
                                StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategyProvider) {
        super(policyId, root, nodeSelector, treeUpdater, nodeEvaluator);
        this.random = random;
        this.totalRiskAllowed = totalRiskAllowed;
        this.nodeSelector = nodeSelector;

        this.inferenceExistingFlowDistribution = strategyProvider.provideInferenceExistingFlowStrategy();
        this.inferenceNonExistingFlowDistribution = strategyProvider.provideInferenceNonExistingFlowStrategy();
        this.explorationExistingFlowDistribution = strategyProvider.provideExplorationExistingFlowStrategy();
        this.explorationNonExistingFlowDistribution = strategyProvider.provideExplorationNonExistingFlowStrategy();
        this.flowOptimizer = strategyProvider.provideFlowOptimizer(random);
    }

    private PlayingDistribution<TAction, TObservation, TSearchNodeMetadata, TState> inferencePolicyBranch() {
        if(tryOptimizeFlow()) {
            return inferenceExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        } else {
            return inferenceNonExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        }
    }

    private PlayingDistribution<TAction, TObservation, TSearchNodeMetadata, TState> explorationPolicyBranch( double temperature) {
        if(tryOptimizeFlow()) {
            return explorationExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        } else {
            return explorationNonExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        }
    }

    private PlayingDistribution<TAction, TObservation, TSearchNodeMetadata, TState> createActionWithDistribution(PolicyStepMode policyStepMode, double temperature) {
        switch (policyStepMode) {
            case EXPLOITATION:
                return inferencePolicyBranch();
            case EXPLORATION:
                return explorationPolicyBranch(temperature);
            default: throw EnumUtils.createExceptionForUnknownEnumValue(policyStepMode);
        }
    }

    public PlayingDistribution<TAction, TObservation, TSearchNodeMetadata, TState> getActionDistributionAndDiscreteAction(StateWrapper<TAction, TObservation,TState> state, PolicyStepMode policyStepMode, double temperature) {
        if(!state.isPlayerTurn()) {
            throw new IllegalStateException("Cannot determine action distribution on opponent's turn");
        }
        try {
            this.playingDistribution = createActionWithDistribution(policyStepMode, temperature);
        return playingDistribution;
        } catch(Exception e) {
            dumpTreeWithFlow();
            throw e;
        }
    }

    public boolean isFlowOptimized() {
        return isFlowOptimized;
    }

    public boolean isRiskIgnored() {
        return totalRiskAllowed >= 1.0;
    }

    public double getTotalRiskAllowed() {
        return totalRiskAllowed;
    }

    private boolean tryOptimizeFlow() {
        if(isRiskIgnored()) {
            isFlowOptimized = false;
            return false;
        }
        if(!isFlowOptimized) {
            var result = flowOptimizer.optimizeFlow(getRoot(), totalRiskAllowed);
            totalRiskAllowed = result.getFirst();
            if(!result.getSecond()) {
                logger.error("Solution to flow optimisation does not exist. Setting allowed risk to 1.0 in state: [" + System.lineSeparator() + getRoot().getStateWrapper().getWrappedState().readableStringRepresentation() + System.lineSeparator() + "] with allowed risk: [" + totalRiskAllowed + "]");
                totalRiskAllowed = 1.0;
                isFlowOptimized = false;
                return false;
            }
            isFlowOptimized = true;
            return true;
        }
        return true;
    }

    private double roundRiskIfBelowZero(double risk, String riskName) {
        if(risk < 0.0 - NUMERICAL_RISK_DIFF_TOLERANCE) {
            if(DEBUG_ENABLED) {
                logger.debug("Risk [" + riskName + "] cannot be negative. Actual value: [" + risk + "]");
            }
            return 0.0;
        } else if(risk < 0.0) {
            if(DEBUG_ENABLED) {
                logger.debug("Rounding risk [{}] with value [{}] to 0.0", riskName, risk);
            }
            return 0.0;
        } else {
            return risk;
        }
    }

    @Override
    public boolean updateTree() {
        isFlowOptimized = false;
        try {
            this.nodeSelector.setAllowedRiskInRoot(this.totalRiskAllowed);
            return super.updateTree();
        } catch(Exception e) {
            dumpTree();
            throw e;
        }
    }

    @Override
    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction action) {
        try {
            if(action.isPlayerAction(policyId) && action != playingDistribution.getExpectedPlayerAction()) {
                throw new IllegalStateException("RiskAverseTree is applied with player action which was not selected by riskAverseTree. Discrepancy.");
            }
            if(action.isPlayerAction(policyId)) {
                latestTreeWithPlayerOnTurn = this.getRoot(); // debug purposes
                subtreeRiskCalculator = playingDistribution.getUsedSubTreeRiskCalculatorSupplierMap().get(action).get();
            }
            isFlowOptimized = false;
            if(!action.isPlayerAction(policyId) && !isRiskIgnored()) {
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

                var riskOfOtherOpponentActions = 0.0;
                for (var entry : getRoot().getChildNodeMap().values()) {
                    if(entry.getAppliedAction() != action) {
                        riskOfOtherOpponentActions += subtreeRiskCalculator.calculateRisk(entry) * entry.getSearchNodeMetadata().getPriorProbability() * playerActionProbability;
                    }
                }

                riskOfOtherOpponentActions = roundRiskIfBelowZero(riskOfOtherOpponentActions, "RiskOfOtherOpponentActions");

                var dividingProbability = (playerActionProbability * opponentActionProbability);
                var oldRisk = totalRiskAllowed;

                if(Arrays.stream(riskEstimatedVector).anyMatch(value -> value > 0.0)) {
                    totalRiskAllowed = (totalRiskAllowed - (riskOfOtherPlayerActions + riskOfOtherOpponentActions)) / dividingProbability;
                    totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
                }

                if(DEBUG_ENABLED) {
                    logger.debug("Playing action: [{}] from actions: [{}]) with distribution: [{}] with minimalRiskReachAbility: [{}]. Risk of other player actions: [{}]. Risk of other Opponent actions: [{}], dividing probability: [{}], old risk: [{}], new risk: [{}]",
                        playingDistribution.getExpectedPlayerAction(),
                        playingDistribution.getActionList().stream().map(Object::toString).reduce((x, y) -> x + ", " + y).orElseThrow(() -> new IllegalStateException("Result of reduce does not exist")),
                        Arrays.toString(playerActionDistribution),
                        Arrays.toString(riskEstimatedVector),
                        riskOfOtherPlayerActions,
                        riskOfOtherOpponentActions,
                        dividingProbability,
                        oldRisk,
                        totalRiskAllowed
                    );
                }
                if(totalRiskAllowed > 1.0 + NUMERICAL_RISK_DIFF_TOLERANCE) {
                    if(DEBUG_ENABLED) {
                        logger.debug("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                    }
                    totalRiskAllowed = 1.0;
                }
                if(DEBUG_ENABLED) {
                    logger.debug("New Global risk: [{}]", totalRiskAllowed);
                }
            }
            return innerApplyAction(action);
        } catch (Exception e) {
            dumpTreeWithFlow();
            throw new IllegalStateException("Applying action to player policy failed. Check that there is consistency between possible playable actions on state and known model probabilities. " +
                "Applying action [" + action + "] to state: [" + System.lineSeparator() +  getRoot().getStateWrapper().getWrappedState().readableStringRepresentation() + "]", e);
        }
    }

    private void dumpTreeWithFlow() {
        if(this.latestTreeWithPlayerOnTurn != null) {
            this.printTreeToFileWithFlowNodesOnly(this.latestTreeWithPlayerOnTurn, "TreeDump_player");
        }
        this.printTreeToFileWithFlowNodesOnly(this.getRoot(), "TreeDump_latest");
    }

    private void dumpTree() {
        this.printTreeToFile(this.getRoot(), "TreeDump_FULL", 1000);
    }


    private void printTreeToFileWithFlowNodesOnly(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName) {
        printTreeToFileInternal(subtreeRoot, fileName, Integer.MAX_VALUE, a -> a.getSearchNodeMetadata().getNodeProbabilityFlow() == null || a.getSearchNodeMetadata().getFlow() != 0);
    }

}
