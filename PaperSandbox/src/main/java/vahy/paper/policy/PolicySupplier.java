package vahy.paper.policy;

import vahy.paper.reinforcement.TrainableApproximator;
import vahy.paper.tree.NodeEvaluator;
import vahy.paper.tree.NodeExpander;
import vahy.paper.tree.NodeSelector;
import vahy.paper.tree.SearchNode;
import vahy.paper.tree.SearchTree;
import vahy.paper.tree.TreeUpdater;
import vahy.paper.tree.OptimalFlowCalculator;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.SplittableRandom;

public class PolicySupplier {

    private final double cpuctParameter;
    private final int treeUpdateCount;
    private final double totalRiskAllowed;
    private final SplittableRandom random;
    private final TrainableApproximator trainableApproximator;
    private final boolean optimizeFlowInSearchTree;

    public PolicySupplier(double cpuctParameter, int treeUpdateCount, double totalRiskAllowed, SplittableRandom random, TrainableApproximator trainableApproximator, boolean optimizeFlowInSearchTree) {
        this.cpuctParameter = cpuctParameter;
        this.treeUpdateCount = treeUpdateCount;
        this.totalRiskAllowed = totalRiskAllowed;
        this.random = random;
        this.trainableApproximator = trainableApproximator;
        this.optimizeFlowInSearchTree = optimizeFlowInSearchTree;
    }

    public PaperPolicyImpl initializePolicy(ImmutableStateImpl initialState) {
        return createPolicy(initialState);
    }

    protected PaperPolicyImpl createPolicy(ImmutableStateImpl initialState) {
        SearchNode node = new SearchNode(initialState, null, null, new DoubleScalarReward(0.0));
        return new PaperPolicyImpl(
            random,
            new SearchTree(
                node,
                new NodeSelector(cpuctParameter, random),
                new NodeExpander(),
                new TreeUpdater(),
                new NodeEvaluator(trainableApproximator),
                new OptimalFlowCalculator(),
                totalRiskAllowed),
            treeUpdateCount,
            optimizeFlowInSearchTree);
    }

}
