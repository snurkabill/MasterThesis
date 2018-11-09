package vahy.paper.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.tree.SearchNode;
import vahy.paper.tree.SearchTree;
import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.paper.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.timer.SimpleTimer;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class PaperPolicyImpl implements PaperPolicy {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicyImpl.class);

    private final SplittableRandom random;
    private final boolean optimizeFlowInTree;
    private final SearchTree searchTree;
    private final TreeUpdateCondition treeUpdateCondition;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    public PaperPolicyImpl(SplittableRandom random, SearchTree searchTree, boolean optimizeFlowInTree, TreeUpdateCondition treeUpdateCondition) {
        this.random = random;
        this.searchTree = searchTree;
        this.treeUpdateCondition = treeUpdateCondition;
        this.optimizeFlowInTree = optimizeFlowInTree;
    }

    public SplittableRandom getRandom() {
        return random;
    }

    @Override
    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        checkStateRoot(gameState);
        ActionType[] allDoableActions = gameState.isOpponentTurn() ? ActionType.environmentActions : ActionType.playerActions;
        double[] vector = new double[allDoableActions.length];
        if(optimizeFlowInTree && !searchTree.isOpponentTurn()) {

            searchTree.optimizeFlow();

            // LALALA code duplication!
            List<ImmutableTuple<ActionType, Double>> actionDoubleList = this.searchTree
                .getRoot()
                .getChildMap()
                .entrySet()
                .stream()
                .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getNodeProbabilityFlow().getSolution()))
                .collect(Collectors.toList());
            for (ImmutableTuple<ActionType, Double> entry : actionDoubleList) {
                int actionIndex = entry.getFirst().getActionIndexAsPlayerAction();
                vector[actionIndex] = entry.getSecond();
            }
        } else {
            List<ImmutableTuple<ActionType, Integer>> actionIntegerList = this.searchTree
                .getRoot()
                .getEdgeMetadataMap()
                .entrySet()
                .stream()
                .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getVisitCount()))
                .collect(Collectors.toList());
            int sum = actionIntegerList.stream().mapToInt(ImmutableTuple::getSecond).sum();
            for (ImmutableTuple<ActionType, Integer> entry : actionIntegerList) {
                int actionIndex = gameState.isOpponentTurn() ? entry.getFirst().getActionIndexAsEnvironmentAction() : entry.getFirst().getActionIndexAsPlayerAction();
                vector[actionIndex] = entry.getSecond() / (double) sum;
            }
        }
        return vector;
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        checkStateRoot(gameState);
        ActionType[] allDoableActions = gameState.isOpponentTurn() ? ActionType.environmentActions : ActionType.playerActions;
        double[] priorProbabilities = new double[allDoableActions.length];
        List<ImmutableTuple<ActionType, Double>> actionDoubleList = this.searchTree
            .getRoot()
            .getEdgeMetadataMap()
            .entrySet()
            .stream()
            .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getPriorProbability()))
            .collect(Collectors.toList());
        for (ImmutableTuple<ActionType, Double> entry : actionDoubleList) {
            int actionIndex = gameState.isOpponentTurn() ? entry.getFirst().getActionIndexAsEnvironmentAction() : entry.getFirst().getActionIndexAsPlayerAction();
            priorProbabilities[actionIndex] = entry.getSecond();
        }
        return priorProbabilities;
    }

    @Override
    public DoubleScalarReward getEstimatedReward(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        checkStateRoot(gameState);
        return searchTree.getRootEstimatedReward();
    }

    @Override
    public double getEstimatedRisk(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        checkStateRoot(gameState);
        return searchTree.getRootEstimatedRisk();
    }

    @Override
    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        expandSearchTree(gameState);
        SearchNode node = searchTree.getRoot();

        if(optimizeFlowInTree) {

//            this.searchTree.getRoot().printTreeToFile("pre", 5);
            searchTree.optimizeFlow();
//            this.searchTree.getRoot().printTreeToFile("post", 10);

            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);

            ActionType[] playerActions = ActionType.playerActions;
            double rand = random.nextDouble();
            double cumulativeSum = 0.0d;

            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                cumulativeSum += actionProbabilityDistribution[i];
                if(rand < cumulativeSum) {
                    return playerActions[i];
                }
            }
            throw new IllegalStateException("Numerically unstable probability calculation");

        } else {
            return node
                .getEdgeMetadataMap()
                .entrySet()
                .stream()
                .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getVisitCount()))
                .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random))
                .getFirst();
        }
    }

    public void updateStateOnOpponentActions(List<ActionType> opponentActionList) {
        for (ActionType action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    private void expandSearchTree(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        checkStateRoot(gameState);
        timer.startTimer();
        treeUpdateCondition.treeUpdateRequired();
        for (int i = 0; treeUpdateCondition.isConditionSatisfied(); i++) {
            logger.trace("Performing tree update for [{}]th iteration", i);
            searchTree.updateTree();
        }
        treeUpdateCondition.treeUpdateFinished();
        timer.stopTimer();

        if (searchTree.getTotalNodesExpanded() == 0) {
            logger.debug("Finished updating search tree. No node was expanded - there is likely strong existing path to final state");
        } else {
            logger.debug(
                "Finished updating search tree with total expanded node count: [{}], " +
                    "total created node count: [{}], " +
                    "max branch factor: [{}], " +
                    "average branch factor [{}] in [{}] seconds, expanded nodes per second: [{}]",
                searchTree.getTotalNodesExpanded(),
                searchTree.getTotalNodesCreated(),
                searchTree.getMaxBranchingFactor(),
                searchTree.calculateAverageBranchingFactor(),
                timer.secondsSpent(),
                timer.samplesPerSec(searchTree.getTotalNodesExpanded()));
        }
    }

    private void checkStateRoot(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree PaperPolicy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
    }

}
