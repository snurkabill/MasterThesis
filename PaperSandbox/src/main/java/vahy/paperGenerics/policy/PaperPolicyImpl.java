package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PolicyMode;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class PaperPolicyImpl<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>
    implements PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicyImpl.class.getName());

    private final List<TAction> playerActions;

    private final SplittableRandom random;
    private final RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> riskAverseSearchTree;

    private final boolean isExplorationDisabled;
    private final double explorationConstant;
    private final double temperature;

    private double[] actionDistribution;
    private boolean hasActionChanged = false;

    public PaperPolicyImpl(Class<TAction> clazz,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree,
                           SplittableRandom random,
                           double explorationConstant,
                           double temperature) {
        this(clazz, treeUpdateCondition, searchTree, random, false, explorationConstant, temperature);
    }

    public PaperPolicyImpl(Class<TAction> clazz,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree,
                           SplittableRandom random) {
        this(clazz, treeUpdateCondition, searchTree, random, true, 0.0, 0.0);
    }

    private PaperPolicyImpl(Class<TAction> clazz,
                            TreeUpdateCondition treeUpdateCondition,
                            RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree,
                            SplittableRandom random,
                            boolean isExplorationDisabled,
                            double explorationConstant,
                            double temperature) {
        super(treeUpdateCondition, searchTree);
        this.random = random;
        this.riskAverseSearchTree = searchTree;
        TAction[] allActions = clazz.getEnumConstants();

        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).collect(Collectors.toCollection(ArrayList::new));

        this.isExplorationDisabled = isExplorationDisabled;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;

        this.actionDistribution = new double[playerActions.size()];
    }

    @Override
    public TReward getEstimatedReward(TState gameState) {
        checkStateRoot(gameState);
        return searchTree.getRoot().getSearchNodeMetadata().getExpectedReward();
    }

    @Override
    public double getEstimatedRisk(TState gameState) {
        checkStateRoot(gameState);
        return searchTree.getRoot().getSearchNodeMetadata().getPredictedRisk();
    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        checkStateRoot(gameState);

        if(!hasActionChanged) {
            throw new IllegalStateException("Action probability distribution second time without changing state");
        }

//        riskAverseSearchTree.optimizeFlow();
//        double[] vector = new double[gameState.isPlayerTurn() ? playerActions.size() : environmentActions.size()];
//        List<ImmutableTuple<TAction, Double>> actionDoubleList = riskAverseSearchTree.isFlowOptimized() ?
//            this.searchTree
//                .getRoot()
//                .getChildNodeStream()
//                .map(x -> new ImmutableTuple<>(x.getAppliedAction(), x.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution()))
//                .collect(Collectors.toList())
//            : getUcbVisitActionProbabilityDistribution();
//        for (ImmutableTuple<TAction, Double> entry : actionDoubleList) {
//            int actionIndex = entry.getFirst().getActionIndexInPossibleActions();
//            vector[actionIndex] = entry.getSecond();
//        }
        return actionDistribution;
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {
        checkStateRoot(gameState);
        expandSearchTree(gameState); //  TODO expand search tree should be enabled in episode simulation

        boolean exploitation = isExplorationDisabled || random.nextDouble() > explorationConstant;

        var actionDistributionAndDiscreteAction = riskAverseSearchTree.getActionDistributionAndDiscreteAction(
            gameState,
            exploitation ? PolicyMode.EXPLOITATION : PolicyMode.EXPLORATION,
            temperature);
        var action = actionDistributionAndDiscreteAction.getFirst();
        actionDistribution = actionDistributionAndDiscreteAction.getSecond();
        hasActionChanged = true;

        if(exploitation) {
            logger.debug("Exploitation action [{}].", action);
        } else {
            logger.debug("Exploration action [{}]", action);
        }
        return action;

//        if(randomDouble > ) {
//
//            actionDistribution = actionDistributionAndDiscreteAction.getSecond();
//            TAction action = actionDistributionAndDiscreteAction.getFirst();
////            TAction discreteAction = sampleDistributionFromExploitingDistribution(gameState);
//            logger.debug("Exploitation action [{}].", action);
//            return action;

//        } else {
//            var actionDistributionAndDiscreteAction = riskAverseSearchTree.getActionDistributionAndDiscreteAction(gameState, PolicyMode.EXPLORATION);
//            TAction[] stateAllowedActions = gameState.getAllPossibleActions();
//            List<TAction> policyAllowedActions = getAllowedActionsForExploration();
//
//            var allowedActions = new LinkedList<TAction>();
//            for (TAction policyAllowedAction : policyAllowedActions) { // N * N
//                for (TAction stateAllowedAction : stateAllowedActions) {
//                    if (stateAllowedAction == policyAllowedAction) {
//                        allowedActions.add(stateAllowedAction);
//                        break;
//                    }
//                }
//            }
//            updateMask(allowedActions);
//
//            double[] actionDistribution = this.getActionProbabilityDistribution(gameState);
//            for (int i = 0; i < actionDistribution.length; i++) {
//                actionDistribution[i] = actionMask[i] ? actionDistribution[i] : 0.0;
//            }
//            RandomDistributionUtils.applyTemperatureNoise(actionDistribution, temperature);
//            RandomDistributionUtils.applySoftmax(actionDistribution);
//            for (int i = 0; i < playerActions.size(); i++) {
//                exploringPlayerDistribution.put(playerActions.get(i), actionDistribution[i]);
//            }
//            riskAverseSearchTree.setPlayerDistribution(exploringPlayerDistribution);
////            logger.info("ActionProbabilityDistribution: [{}]", Arrays.toString(actionDistribution));
////            logger.info("Exponentiation:                [{}]", Arrays.toString(exponentiation));
//            int index = RandomDistributionUtils.getRandomIndexFromDistribution(actionDistribution, random);
//            logger.debug("Exploration action [{}]", playerActions.get(index));
//            return playerActions.get(index);
//        }
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(TState gameState) {
        checkStateRoot(gameState);
        if(gameState.isOpponentTurn()) {
            throw new IllegalStateException("Can't sample opponent's distribution from player's policy");
        }
        double[] priorProbabilities = new double[playerActions.size()];
        List<ImmutableTuple<TAction, Double>> actionDoubleList = this.searchTree
            .getRoot()
            .getChildNodeStream()
            .map(x -> new ImmutableTuple<>(x.getAppliedAction(), x.getSearchNodeMetadata().getPriorProbability()))
            .collect(Collectors.toList());
        for (ImmutableTuple<TAction, Double> entry : actionDoubleList) {
            int actionIndex = entry.getFirst().getActionIndexInPossibleActions();
            priorProbabilities[actionIndex] = entry.getSecond();
        }
        return priorProbabilities;
    }

//    private TAction sampleDistributionFromExploitingDistribution(TState gameState) {
//        double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
//        double rand = random.nextDouble();
//        double cumulativeSum = 0.0d;
//        for (int i = 0; i < actionProbabilityDistribution.length; i++) {
//            cumulativeSum += actionProbabilityDistribution[i];
//            if(rand < cumulativeSum) {
//                return playerActions.get(i);
//            }
//        }
//        throw new IllegalStateException("Numerically unstable probability calculation");
//    }

//    private List<ImmutableTuple<TAction, Double>> getUcbVisitActionProbabilityDistribution() {
//        List<ImmutableTuple<TAction, Double>> nonNormalizedDistr = this.searchTree
//            .getRoot()
//            .getChildNodeStream()
//            .map(x -> new ImmutableTuple<>(x.getAppliedAction(), (double) x.getSearchNodeMetadata().getVisitCounter()))
//            .collect(Collectors.toList());
//
//        double totalSum = this.searchTree
//            .getRoot()
//            .getChildNodeStream()
//            .mapToDouble(x -> x.getSearchNodeMetadata().getVisitCounter())
//            .sum();
//        return nonNormalizedDistr
//            .stream()
//            .map(x -> new ImmutableTuple<>(x.getFirst(), x.getSecond() / totalSum))
//            .collect(Collectors.toList());
//    }

//    private void updateMask(List<TAction> possibleActions) {
//        for (int i = 0; i < playerActions.size(); i++) {
//            this.actionMask[i] = false;
//            for (TAction possibleAction : possibleActions) {
//                if(playerActions.get(i) == possibleAction) {
//                    this.actionMask[i] = true;
//                    break;
//                }
//            }
//        }
//    }
}
