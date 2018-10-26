package vahy.paper.policy;

import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.paper.tree.NodeExpander;
import vahy.paper.tree.NodeSelector;
import vahy.paper.tree.OptimalFlowCalculator;
import vahy.paper.tree.SearchNode;
import vahy.paper.tree.SearchTree;
import vahy.paper.tree.TreeUpdater;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.paper.tree.treeUpdateConditionSupplier.TreeUpdateConditionSupplier;

import java.util.SplittableRandom;

public class PolicySupplier {

    private final double cpuctParameter;
    private final double totalRiskAllowed;
    private final SplittableRandom random;
    private final NodeEvaluator nodeEvaluator;
    private final TreeUpdateConditionSupplier treeUpdateConditionSupplier;
    private final boolean optimizeFlowInSearchTree;

    public PolicySupplier(double cpuctParameter,
                          double totalRiskAllowed,
                          SplittableRandom random,
                          NodeEvaluator nodeEvaluator,
                          TreeUpdateConditionSupplier treeUpdateConditionSupplier,
                          boolean optimizeFlowInSearchTree) {
        this.cpuctParameter = cpuctParameter;
        this.treeUpdateConditionSupplier = treeUpdateConditionSupplier;
        this.totalRiskAllowed = totalRiskAllowed;
        this.random = random;
        this.nodeEvaluator = nodeEvaluator;
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
                nodeEvaluator,
                new OptimalFlowCalculator(),
                totalRiskAllowed),
            optimizeFlowInSearchTree,
            treeUpdateConditionSupplier);
    }

}
