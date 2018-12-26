package vahy.paperOldImpl.policy;

import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.reward.DoubleReward;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperOldImpl.tree.NodeSelector;
import vahy.paperOldImpl.tree.OptimalFlowCalculator;
import vahy.paperOldImpl.tree.SearchNode;
import vahy.paperOldImpl.tree.SearchTree;
import vahy.paperOldImpl.tree.nodeEvaluator.NodeEvaluator;
import vahy.paperOldImpl.tree.nodeExpander.NodeExpander;
import vahy.paperOldImpl.tree.treeUpdater.TreeUpdater;

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

    public PaperPolicyImpl initializePolicy(HallwayStateImpl initialState) {
        return createPolicy(initialState);
    }

    protected PaperPolicyImpl createPolicy(HallwayStateImpl initialState) {
        SearchNode node = new SearchNode(initialState, null, null, new DoubleReward(0.0));
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
