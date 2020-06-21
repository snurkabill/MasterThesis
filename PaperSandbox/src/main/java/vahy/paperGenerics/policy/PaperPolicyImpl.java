package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyStepMode;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionWithRisk;
import vahy.utils.ArrayUtils;

import java.util.Arrays;
import java.util.SplittableRandom;

public class PaperPolicyImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TObservation, TSearchNodeMetadata, TState, PaperPolicyRecord>
    implements PaperPolicy<TAction, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicyImpl.class.getName());
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();

    private final RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> riskAverseSearchTree;

    private final boolean isExplorationDisabled;
    private final double explorationConstant;
    private final double temperature;

    private double[] actionDistribution;
    private boolean hasActionChanged = false;

    public PaperPolicyImpl(int policyId,
                           SplittableRandom random,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree,
                           double explorationConstant,
                           double temperature) {
        this(policyId, random, treeUpdateCondition, searchTree, false, explorationConstant, temperature);
    }

    public PaperPolicyImpl(int policyId,
                           SplittableRandom random,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this(policyId, random, treeUpdateCondition, searchTree, true, 0.0, 0.0);
    }

    private PaperPolicyImpl(int policyId,
                            SplittableRandom random,
                            TreeUpdateCondition treeUpdateCondition,
                            RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree,
                            boolean isExplorationDisabled,
                            double explorationConstant,
                            double temperature) {
        super(policyId, random, treeUpdateCondition, searchTree);
        this.riskAverseSearchTree = searchTree;
//        TAction[] allActions = clazz.getEnumConstants();

//        this.totalPlayerActionCount = (int) Arrays.stream(allActions).filter(x -> x.isPlayerAction(policyId)).count();
        this.isExplorationDisabled = isExplorationDisabled;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
    }

    @Override
    public double getEstimatedReward(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        return riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedReward();
    }

    @Override
    public double getEstimatedRisk(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        return riskAverseSearchTree.getRoot().getSearchNodeMetadata().getPredictedRisk();
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        return innerActionProbability();
    }

    private double[] innerActionProbability() {
        if(!hasActionChanged) {
            throw new IllegalStateException("Action probability distribution second time without changing state");
        }
        return ArrayUtils.cloneArray(actionDistribution);
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        if(!gameState.isPlayerTurn()) {
            throw new IllegalStateException("Player is not on turn.");
        }
        checkStateRoot(gameState);
        expandSearchTree(gameState); //  TODO expand search tree should be enabled in episode simulation

        if(actionDistribution == null) {
            lazyDistributionInit(gameState);
        }

        boolean exploitation = isExplorationDisabled || random.nextDouble() > explorationConstant;

        var actionDistributionAndDiscreteAction = riskAverseSearchTree.getActionDistributionAndDiscreteAction(
            gameState,
            exploitation ? PolicyStepMode.EXPLOITATION : PolicyStepMode.EXPLORATION,
            temperature);
        var action = actionDistributionAndDiscreteAction.getExpectedPlayerAction();
        fillProbabilityActionDistribution(actionDistributionAndDiscreteAction);
        hasActionChanged = true;
        if(DEBUG_ENABLED) {
            if(exploitation) {
                logger.debug("Exploitation action [{}].", action);
            } else {
                logger.debug("Exploration action [{}]", action);
            }
        }
        return action;
    }

    private void fillProbabilityActionDistribution(PlayingDistributionWithRisk<TAction, TObservation, TSearchNodeMetadata, TState> actionDistributionAndDiscreteAction) {
        var actionList = actionDistributionAndDiscreteAction.getActionList();
        var actionProbabilities = actionDistributionAndDiscreteAction.getPlayerDistribution();
        Arrays.fill(actionDistribution, 0.0);
        for (int i = 0; i < actionList.size(); i++) {
            var element = actionList.get(i);
            var probability = actionProbabilities[i];
            actionDistribution[element.getLocalIndex()] = probability;
        }
    }

    private void lazyDistributionInit(StateWrapper<TAction, TObservation, TState> gameState) {
        var actionArray = gameState.getAllPossibleActions();
        if(actionArray.length == 0) {
            throw new IllegalStateException("There must be at least one playable action.");
        }
        actionDistribution = new double[actionArray[0].getCountOfAllActionsFromSameEntity()];
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        return innerPriorProbabilityDistribution(gameState);
    }

    private double[] innerPriorProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        if(!gameState.isPlayerTurn()) {
            throw new IllegalStateException("Player is not on turn.");
        }
        double[] priorProbabilities = new double[gameState.getAllPossibleActions().length];
        for (var entry : this.riskAverseSearchTree.getRoot().getChildNodeMap().entrySet()) {
            var action = entry.getValue().getAppliedAction();
            int actionIndex = action.getLocalIndex();
            priorProbabilities[actionIndex] = entry.getValue().getSearchNodeMetadata().getPriorProbability();
        }

        return priorProbabilities;
    }

    @Override
    public double getInnerRiskAllowed() {
        return this.riskAverseSearchTree.getTotalRiskAllowed();
    }

    @Override
    public PaperPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        return new PaperPolicyRecord(
            innerPriorProbabilityDistribution(gameState),
            gameState.isPlayerTurn() ? innerActionProbability() : innerPriorProbabilityDistribution(gameState),
            riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedReward(),
            riskAverseSearchTree.getRoot().getSearchNodeMetadata().getSumOfRisk(),
            riskAverseSearchTree.getTotalRiskAllowed(),
            riskAverseSearchTree.getTotalNodesExpanded());
    }

}
