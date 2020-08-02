package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class PaperPolicyImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicyImpl.class.getName());
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();

    private final RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> riskAverseSearchTree;

    private final double temperature;

//    private double[] actionDistribution;
//    private boolean hasActionChanged = false;

    public PaperPolicyImpl(int policyId,
                           SplittableRandom random,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this(policyId, random, treeUpdateCondition, searchTree, 0.0, 0.0);
    }

    public PaperPolicyImpl(int policyId,
                            SplittableRandom random,
                            TreeUpdateCondition treeUpdateCondition,
                            RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree,
                            double explorationConstant,
                            double temperature) {
        super(policyId, random, explorationConstant, treeUpdateCondition, searchTree);
        this.riskAverseSearchTree = searchTree;
        this.temperature = temperature;
    }

//    @Override
//    public double getEstimatedReward(StateWrapper<TAction, TObservation, TState> gameState) {
//        checkStateRoot(gameState);
//        return riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedReward();
//    }
//
//    @Override
//    public double getEstimatedRisk(StateWrapper<TAction, TObservation, TState> gameState) {
//        checkStateRoot(gameState);
//        return riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedRisk();
//    }
//
//    @Override
//    public double[] getPriorActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
//        checkStateRoot(gameState);
//        return innerPriorProbabilityDistribution(gameState);
//    }
//
//    @Override
//    public double getInnerRiskAllowed() {
//        return this.riskAverseSearchTree.getTotalRiskAllowed();
//    }
//
//
//
//    private double[] innerActionProbability() {
//        if(!hasActionChanged) {
//            throw new IllegalStateException("Action probability distribution second time without changing state");
//        }
//        return ArrayUtils.cloneArray(actionDistribution);
//    }

//    @Override
//    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
//        if(!gameState.isPlayerTurn()) {
//            throw new IllegalStateException("Player is not on turn.");
//        }
//        checkStateRoot(gameState);
//        expandSearchTree(gameState); //  TODO expand search tree should be enabled in episode simulation
//
//        if(actionDistribution == null) {
//            lazyDistributionInit(gameState);
//        }
//
//        boolean exploitation = isExplorationDisabled || random.nextDouble() > explorationConstant;
//
//        var actionDistributionAndDiscreteAction = riskAverseSearchTree.getActionDistributionAndDiscreteAction(
//            gameState,
//            exploitation ? PolicyStepMode.EXPLOITATION : PolicyStepMode.EXPLORATION,
//            temperature);
//        var action = actionDistributionAndDiscreteAction.getExpectedPlayerAction();
//        fillProbabilityActionDistribution(actionDistributionAndDiscreteAction);
//        hasActionChanged = true;
//        if(DEBUG_ENABLED) {
//            if(exploitation) {
//                logger.debug("Exploitation action [{}].", action);
//            } else {
//                logger.debug("Exploration action [{}]", action);
//            }
//        }
//        return action;
//    }

    @Override
    protected PlayingDistribution<TAction> inferenceBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        var distributionWithRisk = riskAverseSearchTree.inferencePolicyBranch();
        return distributionWithRisk;
    }

    @Override
    protected PlayingDistribution<TAction> explorationBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        var distributionWithRisk = riskAverseSearchTree.explorationPolicyBranch(temperature);
        return distributionWithRisk;
    }

//    private void fillProbabilityActionDistribution(PlayingDistributionWithRisk<TAction, TObservation, TSearchNodeMetadata, TState> actionDistributionAndDiscreteAction) {
//        var actionList = actionDistributionAndDiscreteAction.getActionList();
//        var actionProbabilities = actionDistributionAndDiscreteAction.getPlayerDistribution();
//        Arrays.fill(actionDistribution, 0.0);
//        for (int i = 0; i < actionList.size(); i++) {
//            var element = actionList.get(i);
//            var probability = actionProbabilities[i];
//            actionDistribution[element.getLocalIndex()] = probability;
//        }
//    }

//    private void lazyDistributionInit(StateWrapper<TAction, TObservation, TState> gameState) {
//        var actionArray = gameState.getAllPossibleActions();
//        if(actionArray.length == 0) {
//            throw new IllegalStateException("There must be at least one playable action.");
//        }
//        actionDistribution = new double[actionArray[0].getCountOfAllActionsFromSameEntity()];
//    }
//


//    private double[] innerPriorProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
//        if(!gameState.isPlayerTurn()) {
//            throw new IllegalStateException("Player is not on turn.");
//        }
//        double[] priorProbabilities = new double[gameState.getAllPossibleActions().length];
//        for (var entry : this.riskAverseSearchTree.getRoot().getChildNodeMap().entrySet()) {
//            var action = entry.getValue().getAppliedAction();
//            int actionIndex = action.getLocalIndex();
//            priorProbabilities[actionIndex] = entry.getValue().getSearchNodeMetadata().getPriorProbability();
//        }
//
//        return priorProbabilities;
//    }

    @Override
    public PaperPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);

        var distribution = new double[countOfAllActions];
        distribution[playingDistribution.getPlayedAction().ordinal()] = 1.0;
        return new PaperPolicyRecord(
            distribution,
            playingDistribution.getExpectedReward(),
            riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedRisk()[gameState.getInGameEntityId()],
            riskAverseSearchTree.getTotalRiskAllowed(),
            riskAverseSearchTree.getTotalNodesExpanded());
    }

}
