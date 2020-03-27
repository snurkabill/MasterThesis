package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PolicyStepMode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.flowOptimizer.AbstractFlowOptimizer;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistribution;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.SplittableRandom;

public class RiskAverseSearchTree<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends SearchTreeImpl<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(RiskAverseSearchTree.class);

    public static final double NUMERICAL_RISK_DIFF_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_PROBABILITY_TOLERANCE = Math.pow(10, -13);
    public static final double NUMERICAL_ACTION_RISK_TOLERANCE = Math.pow(10, -13);
    public static final double INVALID_TEMPERATURE_VALUE = -Double.MAX_VALUE;

    private final SplittableRandom random;

    private SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> latestTreeWithPlayerOnTurn = null;

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    private PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playingDistribution;

    private final RiskAverseNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector;

    private final PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> inferenceExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> inferenceNonExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> explorationExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> explorationNonExistingFlowDistribution;
    private final AbstractFlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> flowOptimizer;


    public RiskAverseSearchTree(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root,
                                RiskAverseNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                SplittableRandom random,
                                double totalRiskAllowed,
                                StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategyProvider) {
        super(root, nodeSelector, treeUpdater, nodeEvaluator);
        this.random = random;
        this.totalRiskAllowed = totalRiskAllowed;
        this.nodeSelector = nodeSelector;

        this.inferenceExistingFlowDistribution = strategyProvider.provideInferenceExistingFlowStrategy();
        this.inferenceNonExistingFlowDistribution = strategyProvider.provideInferenceNonExistingFlowStrategy();
        this.explorationExistingFlowDistribution = strategyProvider.provideExplorationExistingFlowStrategy();
        this.explorationNonExistingFlowDistribution = strategyProvider.provideExplorationNonExistingFlowStrategy();
        this.flowOptimizer = strategyProvider.provideFlowOptimizer(random);
    }

    private PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> inferencePolicyBranch() {
        if(tryOptimizeFlow()) {
            return inferenceExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        } else {
            return inferenceNonExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        }
    }

    private PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> explorationPolicyBranch( double temperature) {
        if(tryOptimizeFlow()) {
            return explorationExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        } else {
            return explorationNonExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        }
    }

    private PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createActionWithDistribution(PolicyStepMode policyStepMode, double temperature) {
        switch (policyStepMode) {
            case EXPLOITATION:
                return inferencePolicyBranch();
            case EXPLORATION:
                return explorationPolicyBranch(temperature);
            default: throw EnumUtils.createExceptionForUnknownEnumValue(policyStepMode);
        }
    }

    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getActionDistributionAndDiscreteAction(TState state, PolicyStepMode policyStepMode, double temperature) {
        if(state.isOpponentTurn()) {
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

    private double roundRiskIfBelowZero(double risk, String riskName) {
        if(risk < 0.0 - NUMERICAL_RISK_DIFF_TOLERANCE) {
            if(logger.isDebugEnabled()) {
                logger.debug("Risk [" + riskName + "] cannot be negative. Actual value: [" + risk + "]");
            }
            return 0.0;
        } else if(risk < 0.0) {
            if(logger.isDebugEnabled()) {
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
    public StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction action) {
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

                if(Arrays.stream(riskEstimatedVector).anyMatch(value -> value > 0.0)) {
                    totalRiskAllowed = (totalRiskAllowed - (riskOfOtherPlayerActions + riskOfOtherOpponentActions)) / dividingProbability;
                    totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
                }

                if(logger.isDebugEnabled()) {
                    logger.debug("Playing action: [{}] from actions: [{}]) with distribution: [{}] with minimalRiskReachAbility: [{}]. Risk of other player actions: [{}]. Risk of other Opponent actions: [{}], dividing probability: [{}], old risk: [{}], new risk: [{}]",
                        playingDistribution.getExpectedPlayerAction(),
                        Arrays.stream(action.getAllPlayerActions()).map(Object::toString).reduce((x, y) -> x + ", " + y).orElseThrow(() -> new IllegalStateException("Result of reduce does not exist")),
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
                    if(logger.isDebugEnabled()) {
                        logger.debug("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
                    }
                    totalRiskAllowed = 1.0;
                }
                if(logger.isDebugEnabled()) {
                    logger.debug("New Global risk: [{}]", totalRiskAllowed);
                }
            }
            return innerApplyAction(action);
        } catch(Exception e) {
            dumpTreeWithFlow();
            throw e;
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


    private void printTreeToFileWithFlowNodesOnly(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName) {
        printTreeToFileInternal(subtreeRoot, fileName, Integer.MAX_VALUE, a -> a.getSearchNodeMetadata().getNodeProbabilityFlow() == null || a.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() != 0);
    }

}
