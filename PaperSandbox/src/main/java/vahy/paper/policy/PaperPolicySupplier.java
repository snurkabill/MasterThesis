package vahy.paper.policy;

import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paper.tree.NodeSelector;
import vahy.paper.tree.OptimalFlowCalculator;
import vahy.paper.tree.SearchNode;
import vahy.paper.tree.SearchTree;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.paper.tree.nodeExpander.NodeExpander;
import vahy.paper.tree.treeUpdater.TreeUpdater;

import java.util.SplittableRandom;

public class PaperPolicySupplier {

    private final double cpuctParameter;
    private final double totalRiskAllowed;
    private final SplittableRandom random;
    private final NodeEvaluator nodeEvaluator;
    private final NodeExpander nodeExpander;
    private final TreeUpdater treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final boolean optimizeFlowInSearchTree;

    public PaperPolicySupplier(double cpuctParameter,
                               double totalRiskAllowed,
                               SplittableRandom random,
                               NodeEvaluator nodeEvaluator,
                               NodeExpander nodeExpander,
                               TreeUpdater treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory,
                               boolean optimizeFlowInSearchTree) {
        this.cpuctParameter = cpuctParameter;
        this.nodeExpander = nodeExpander;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
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
                nodeExpander,
                treeUpdater,
                nodeEvaluator,
                new OptimalFlowCalculator(),
                totalRiskAllowed),
            optimizeFlowInSearchTree,
            treeUpdateConditionFactory.create());
    }

}
