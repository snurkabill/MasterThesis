package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.flowOptimizer.AbstractFlowOptimizer;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionWithActionMap;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;

import java.util.Map;
import java.util.SplittableRandom;

public class RiskAverseSearchTree<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
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

    private boolean isFlowOptimized = false;
    private double totalRiskAllowed;

    private double cumulativeDenominator;
    private double cumulativeNominator;
    private double anyRiskEstimated;

    private PlayingDistribution<TAction> playingDistribution;
    private SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRiskCalculator;

    private final RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector;

    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> inferenceExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> inferenceNonExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> explorationExistingFlowDistribution;
    private final PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> explorationNonExistingFlowDistribution;
    private final AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata, TState> flowOptimizer;


    public RiskAverseSearchTree(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root,
                                RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                                TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                                NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                SplittableRandom random,
                                double totalRiskAllowed,
                                StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategyProvider) {
        super(searchNodeFactory, root, nodeSelector, treeUpdater, nodeEvaluator);
        this.random = random;
        this.totalRiskAllowed = totalRiskAllowed;
        this.nodeSelector = nodeSelector;

        this.inferenceExistingFlowDistribution = strategyProvider.provideInferenceExistingFlowStrategy();
        this.inferenceNonExistingFlowDistribution = strategyProvider.provideInferenceNonExistingFlowStrategy();
        this.explorationExistingFlowDistribution = strategyProvider.provideExplorationExistingFlowStrategy();
        this.explorationNonExistingFlowDistribution = strategyProvider.provideExplorationNonExistingFlowStrategy();
        this.flowOptimizer = strategyProvider.provideFlowOptimizer(random);
        this.subtreeRiskCalculator = strategyProvider.provideRiskCalculator().get();
    }

    public PlayingDistribution<TAction> inferencePolicyBranch() {
        if(!isRiskIgnored()) {
            updateRiskLevel();
        }
        if(tryOptimizeFlow()) {
            playingDistribution = inferenceExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        } else {
            playingDistribution = inferenceNonExistingFlowDistribution.createDistribution(getRoot(), INVALID_TEMPERATURE_VALUE, random, totalRiskAllowed);
        }
        return playingDistribution;
    }

    public PlayingDistribution<TAction> explorationPolicyBranch(double temperature) {
        if(!isRiskIgnored()) {
            updateRiskLevel();
        }
        if(tryOptimizeFlow()) {
            playingDistribution = explorationExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        } else {
            playingDistribution = explorationNonExistingFlowDistribution.createDistribution(getRoot(), temperature, random, totalRiskAllowed);
        }
        return playingDistribution;
    }

    public boolean isRiskIgnored() {
        return totalRiskAllowed >= 1.0;
    }

    public double getTotalRiskAllowed() {
        return totalRiskAllowed;
    }

    private boolean tryOptimizeFlow() {
        if(isRiskIgnored()) {
            logger.debug("Risk ignored.");
            isFlowOptimized = false;
            return false;
        }
        logger.debug("Applying risk.");
        if(!isFlowOptimized) {
            var result = flowOptimizer.optimizeFlow(getRoot(), totalRiskAllowed);
            totalRiskAllowed = result.getFirst();
            if(!result.getSecond()) {
                logger.debug("Solution to flow optimisation does not exist. Setting allowed risk to 1.0 in state: [" + System.lineSeparator() + getRoot().getStateWrapper().getWrappedState().readableStringRepresentation() + System.lineSeparator() + "] with allowed risk: [" + totalRiskAllowed + "]");
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
    public boolean expandTree() {
        isFlowOptimized = false;
        try {
            this.nodeSelector.setAllowedRiskInRoot(this.totalRiskAllowed);
            return super.expandTree();
        } catch(Exception e) {
            dumpTree();
            throw e;
        }
    }

    private void updateRiskLevel() {
        if(anyRiskEstimated > 0.0) {
            if(cumulativeNominator > totalRiskAllowed) {
                totalRiskAllowed = 0;
            } else {
//                var gamma = 0.9;
//                var totalRiskAllowedNewValue = (totalRiskAllowed - cumulativeNominator) / cumulativeDenominator;
//                var totalRiskAllowedDiff = totalRiskAllowedNewValue - totalRiskAllowed;
//                var gammedDiff = totalRiskAllowedNewValue + gamma * totalRiskAllowedDiff;
////                totalRiskAllowed = totalRiskAllowedNewValue;
//                totalRiskAllowed = gammedDiff;
                totalRiskAllowed = (totalRiskAllowed - cumulativeNominator) / cumulativeDenominator;
                totalRiskAllowed = roundRiskIfBelowZero(totalRiskAllowed, "TotalRiskAllowed");
            }
        }
        if(TRACE_ENABLED) {
            logger.trace("Total riskAllowed: [{}]. CumulativeNominator: [{}], CumulativeDenominator: [{}], AnyRiskEstimated: [{}]", totalRiskAllowed, cumulativeNominator, cumulativeDenominator, anyRiskEstimated);
        }
        if(totalRiskAllowed > 1.0 - NUMERICAL_RISK_DIFF_TOLERANCE) {
            if(DEBUG_ENABLED) {
                logger.debug("Risk [" + totalRiskAllowed + "] cannot be higher than 1.0");
            }
            totalRiskAllowed = 1.0;
        }
        if(DEBUG_ENABLED) {
            logger.debug("New Global risk: [{}]", totalRiskAllowed);
        }
        cumulativeNominator = 0.0;
        cumulativeDenominator = 0.0;
        anyRiskEstimated = 0.0;
    }


    private void processPlayerAction(TAction action) {
        if(TRACE_ENABLED) {
            logger.trace("Processing player action: [{}]", action);
        }
        var playerActionDistribution = ((PlayingDistributionWithActionMap<TAction>) playingDistribution).getActionMap(); // TODO: casting is little dirty here.
        if(TRACE_ENABLED) {
            logger.trace("Player distribution [{}]", playerActionDistribution.toString());
        }
        var riskOfOtherPlayerActions = 0.0d;
        for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : getRoot().getChildNodeMap().entrySet()) {
            var childRisk = subtreeRiskCalculator.calculateRisk(entry.getValue());
            childRisk = roundRiskIfBelowZero(childRisk, "RiskOfPlayerAction");
            if(TRACE_ENABLED) {
                logger.trace("For action [{}], child risk: [{}]",entry.getKey(), childRisk);
            }
            anyRiskEstimated += childRisk;
            if(entry.getKey().ordinal() != action.ordinal()) {
                riskOfOtherPlayerActions += childRisk * playerActionDistribution.get(entry.getKey());
            }
        }
        cumulativeNominator += riskOfOtherPlayerActions;
        if(cumulativeDenominator == 0.0) {
            cumulativeDenominator = playerActionDistribution.get(action);
        } else {
            cumulativeDenominator *= playerActionDistribution.get(action);
        }
        if(TRACE_ENABLED) {
            logger.trace("After process: Cumulative Nominator: [{}], Cumulative denominator: [{}], AnyRiskEstimated: [{}]", cumulativeNominator, cumulativeDenominator, anyRiskEstimated);
        }

    }

    private void processOpponentAction(TAction action) {
        if(TRACE_ENABLED) {
            logger.trace("Processing opponent action: [{}]", action);
        }
        if(getRoot().getChildNodeMap().containsKey(action)) {
            var riskOfOtherOpponentActions = 0.0;
            for (var entry : getRoot().getChildNodeMap().entrySet()) {
                var childRisk = subtreeRiskCalculator.calculateRisk(entry.getValue());
                childRisk = roundRiskIfBelowZero(childRisk, "RiskOfOpponentAction");
                if(TRACE_ENABLED) {
                    logger.trace("For action [{}], child risk: [{}]", entry.getKey(), childRisk);
                }
                anyRiskEstimated += childRisk;
                if(entry.getKey().ordinal() != action.ordinal()) {
                    riskOfOtherOpponentActions += childRisk * entry.getValue().getSearchNodeMetadata().getPriorProbability() * cumulativeDenominator;
                }
            }
            cumulativeNominator += riskOfOtherOpponentActions;
            if(cumulativeDenominator == 0) {
                cumulativeDenominator = getRoot().getSearchNodeMetadata().getChildPriorProbabilities().get(action);
            } else {
                cumulativeDenominator *= getRoot().getSearchNodeMetadata().getChildPriorProbabilities().get(action);
            }
        } else {
            // TODO: what to do here? do nothing?
            logger.trace("WHAT TO DO HERE");
        }
        if(TRACE_ENABLED) {
            logger.trace("After process: Cumulative Nominator: [{}], Cumulative denominator: [{}], AnyRiskEstimated: [{}]", cumulativeNominator, cumulativeDenominator, anyRiskEstimated);
        }
    }

    @Override
    public void applyAction(TAction action) {
        checkApplicableAction(action);
        isFlowOptimized = false;

        try {
//            if(getRoot().isPlayerTurn() && action != playingDistributionWithRisk.getPlayedAction()) {
//                throw new IllegalStateException("RiskAverseTree is applied with player action which was not selected by riskAverseTree. Discrepancy.");
//            }
            if(!isRiskIgnored()) {
                if(getRoot().isPlayerTurn()) {
                    processPlayerAction(action);
                } else {
                    processOpponentAction(action);
                }
            }
            innerApplyAction(action);
        } catch (Exception e) {
            dumpTreeWithFlow();
            throw new IllegalStateException("Applying action to player policy failed. Check that there is consistency between possible playable actions on state and known model probabilities. " +
                "Applying action [" + action + "] to state: [" + System.lineSeparator() +  getRoot().getStateWrapper().getWrappedState().readableStringRepresentation() + "]", e);
        }
    }

    private void dumpTreeWithFlow() {
        this.printTreeToFileWithFlowNodesOnly(this.getRoot(), "TreeDump_latest");
    }

    private void dumpTree() {
        this.printTreeToFile(this.getRoot(), "TreeDump_FULL", 1000);
    }


    private void printTreeToFileWithFlowNodesOnly(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot, String fileName) {
        printTreeToFileInternal(subtreeRoot, fileName, Integer.MAX_VALUE, a -> a.getSearchNodeMetadata().getNodeProbabilityFlow() == null || a.getSearchNodeMetadata().getFlow() != 0);
    }

}
