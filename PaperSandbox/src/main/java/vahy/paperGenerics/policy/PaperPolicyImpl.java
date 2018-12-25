package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.RiskAverseSearchTree;
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

    private final List<TAction> playerActions;
    private final List<TAction> environmentActions;
    private final SplittableRandom random;
    private final RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> riskAverseSearchTree;
    private final OptimalFlowCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> optimalFlowCalculator = new OptimalFlowCalculator<>(); // pass in constructor

    public PaperPolicyImpl(Class<TAction> clazz,
                           TreeUpdateCondition treeUpdateCondition,
                           RiskAverseSearchTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree,
                           SplittableRandom random) {
        super(treeUpdateCondition, searchTree);
        this.random = random;
        this.riskAverseSearchTree = searchTree;

        TAction[] allActions = clazz.getEnumConstants();
//        Stream<TAction> tActionStream = Arrays.stream(allActions).filter(Action::isPlayerAction);
//        this.playerActions = tActionStream.<TAction>toArray(size -> ReflectionHacks.arrayFromGenericClass(clazz, size));
//        this.environmentActions = Arrays.stream(allActions).filter(x -> !x.isPlayerAction()).toArray(size -> ReflectionHacks.arrayFromGenericClass(clazz, size));

        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).collect(Collectors.toCollection(ArrayList::new));
        this.environmentActions = Arrays.stream(allActions).filter(x -> !x.isPlayerAction()).collect(Collectors.toCollection(ArrayList::new));

    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        checkStateRoot(gameState);
        optimizeFlow();
        double[] vector = new double[gameState.isPlayerTurn() ? playerActions.size() : environmentActions.size()];
        List<ImmutableTuple<TAction, Double>> actionDoubleList = this.searchTree
            .getRoot()
            .getChildNodeStream()
            .map(x -> new ImmutableTuple<>(x.getAppliedAction(), x.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution()))
            .collect(Collectors.toList());
        for (ImmutableTuple<TAction, Double> entry : actionDoubleList) {
            int actionIndex = entry.getFirst().getActionIndexInPossibleActions();
            vector[actionIndex] = entry.getSecond();
        }
        return vector;
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {
        checkStateRoot(gameState);
        expandSearchTree(gameState);
        optimizeFlow();
        double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
        double rand = random.nextDouble();
        double cumulativeSum = 0.0d;
        for (int i = 0; i < actionProbabilityDistribution.length; i++) {
            cumulativeSum += actionProbabilityDistribution[i];
            if(rand < cumulativeSum) {
                return playerActions.get(i);
            }
        }
        throw new IllegalStateException("Numerically unstable probability calculation");
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(TState gameState) {
        checkStateRoot(gameState);
        double[] priorProbabilities = new double[gameState.isPlayerTurn() ? playerActions.size() : environmentActions.size()];
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

    public void optimizeFlow() {
        if(!riskAverseSearchTree.isFlowOptimized()) {
            optimalFlowCalculator.calculateFlow(searchTree.getRoot(), riskAverseSearchTree.getTotalRiskAllowed());
            riskAverseSearchTree.setFlowOptimized(true);
        }
    }
}
