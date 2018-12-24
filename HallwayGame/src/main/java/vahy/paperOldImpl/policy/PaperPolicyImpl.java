package vahy.paperOldImpl.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.reward.DoubleReward;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.paperOldImpl.tree.SearchNode;
import vahy.paperOldImpl.tree.SearchTree;
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
    public double[] getActionProbabilityDistribution(HallwayStateImpl gameState) {
        checkStateRoot(gameState);
        HallwayAction[] allDoableActions = gameState.isOpponentTurn() ? HallwayAction.environmentActions : HallwayAction.playerActions;
        double[] vector = new double[allDoableActions.length];
        if(optimizeFlowInTree && !searchTree.isOpponentTurn()) {

            searchTree.optimizeFlow();

            // LALALA code duplication!
            List<ImmutableTuple<HallwayAction, Double>> actionDoubleList = this.searchTree
                .getRoot()
                .getChildMap()
                .entrySet()
                .stream()
                .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getNodeProbabilityFlow().getSolution()))
                .collect(Collectors.toList());
            for (ImmutableTuple<HallwayAction, Double> entry : actionDoubleList) {
                int actionIndex = entry.getFirst().getActionIndexAsPlayerAction();
                vector[actionIndex] = entry.getSecond();
            }
        } else {
            List<ImmutableTuple<HallwayAction, Integer>> actionIntegerList = this.searchTree
                .getRoot()
                .getEdgeMetadataMap()
                .entrySet()
                .stream()
                .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getVisitCount()))
                .collect(Collectors.toList());
            int sum = actionIntegerList.stream().mapToInt(ImmutableTuple::getSecond).sum();
            for (ImmutableTuple<HallwayAction, Integer> entry : actionIntegerList) {
                int actionIndex = gameState.isOpponentTurn() ? entry.getFirst().getActionIndexAsEnvironmentAction() : entry.getFirst().getActionIndexAsPlayerAction();
                vector[actionIndex] = entry.getSecond() / (double) sum;
            }
        }
        return vector;
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(HallwayStateImpl gameState) {
        checkStateRoot(gameState);
        HallwayAction[] allDoableActions = gameState.isOpponentTurn() ? HallwayAction.environmentActions : HallwayAction.playerActions;
        double[] priorProbabilities = new double[allDoableActions.length];
        List<ImmutableTuple<HallwayAction, Double>> actionDoubleList = this.searchTree
            .getRoot()
            .getEdgeMetadataMap()
            .entrySet()
            .stream()
            .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getPriorProbability()))
            .collect(Collectors.toList());
        for (ImmutableTuple<HallwayAction, Double> entry : actionDoubleList) {
            int actionIndex = gameState.isOpponentTurn() ? entry.getFirst().getActionIndexAsEnvironmentAction() : entry.getFirst().getActionIndexAsPlayerAction();
            priorProbabilities[actionIndex] = entry.getSecond();
        }
        return priorProbabilities;
    }

    @Override
    public DoubleReward getEstimatedReward(HallwayStateImpl gameState) {
        checkStateRoot(gameState);
        return searchTree.getRootEstimatedReward();
    }

    @Override
    public double getEstimatedRisk(HallwayStateImpl gameState) {
        checkStateRoot(gameState);
        return searchTree.getRootEstimatedRisk();
    }

    @Override
    public HallwayAction getDiscreteAction(HallwayStateImpl gameState) {
        expandSearchTree(gameState);
        SearchNode node = searchTree.getRoot();

        if(optimizeFlowInTree) {

//            this.searchTree.getRoot().printTreeToFile("pre", 5000);
            searchTree.optimizeFlow();
//            this.searchTree.getRoot().printTreeToFile("post", 10);

            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);

            HallwayAction[] playerActions = HallwayAction.playerActions;
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

    public void updateStateOnPlayedActions(List<HallwayAction> opponentActionList) {
        for (HallwayAction action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    private void expandSearchTree(HallwayStateImpl gameState) {
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
                timer.getTotalTimeInSeconds(),
                timer.samplesPerSec(searchTree.getTotalNodesExpanded()));
        }
    }

    private void checkStateRoot(HallwayStateImpl gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree PaperPolicy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
    }

}
