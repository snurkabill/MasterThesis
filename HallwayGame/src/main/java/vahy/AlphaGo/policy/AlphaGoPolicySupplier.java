package vahy.AlphaGo.policy;

import vahy.AlphaGo.reinforcement.AlphaGoTrainableApproximator;
import vahy.AlphaGo.tree.AlphaGoNodeEvaluator;
import vahy.AlphaGo.tree.AlphaGoNodeExpander;
import vahy.AlphaGo.tree.AlphaGoNodeSelector;
import vahy.AlphaGo.tree.AlphaGoSearchNode;
import vahy.AlphaGo.tree.AlphaGoSearchTree;
import vahy.AlphaGo.tree.AlphaGoTreeUpdater;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.SplittableRandom;

public class AlphaGoPolicySupplier {

    private final double cpuctParameter;
    private final int treeUpdateCount;
    private final double discountFactor;
    private final SplittableRandom random;
    private final AlphaGoTrainableApproximator trainableApproximator;

    public AlphaGoPolicySupplier(double cpuctParameter, int treeUpdateCount, double discountFactor, SplittableRandom random, AlphaGoTrainableApproximator trainableApproximator) {
        this.cpuctParameter = cpuctParameter;
        this.treeUpdateCount = treeUpdateCount;
        this.discountFactor = discountFactor;
        this.random = random;
        this.trainableApproximator = trainableApproximator;
    }

    public AlphaGoPolicyImpl initializePolicy(ImmutableStateImpl initialState) {
        return createPolicy(initialState);
    }

    protected AlphaGoPolicyImpl createPolicy(ImmutableStateImpl initialState) {
        AlphaGoSearchNode node = new AlphaGoSearchNode(initialState, null, null, new DoubleScalarReward(0.0));
        return new AlphaGoPolicyImpl(
            random,
            new AlphaGoSearchTree(
                node,
                new AlphaGoNodeSelector(cpuctParameter, random),
                new AlphaGoNodeExpander(),
                new AlphaGoTreeUpdater(),
                new AlphaGoNodeEvaluator(trainableApproximator)),
            treeUpdateCount);
    }

}
