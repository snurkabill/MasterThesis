package vahy.paper.policy;

import vahy.paper.reinforcement.TrainableApproximator;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.paper.tree.nodeEvaluator.ApproximatorBasedNodeEvaluator;
import vahy.paper.tree.treeUpdateConditionSupplier.TreeUpdateConditionSupplier;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class PaperTrainablePolicySupplier extends PolicySupplier {

    private final SplittableRandom random;
    private final double explorationConstant;
    private final double temperature;

    private TrainableApproximator trainableRewardApproximator;

    public PaperTrainablePolicySupplier(SplittableRandom random,
                                        double explorationConstant,
                                        double temperature,
                                        double totalRiskAllowed,
                                        ApproximatorBasedNodeEvaluator nodeEvaluator,
                                        TreeUpdateConditionSupplier treeUpdateConditionSupplier,
                                        double cpuctParameter,
                                        boolean optimizeFlowInSearchTree) {
        super(cpuctParameter, totalRiskAllowed, random, nodeEvaluator, treeUpdateConditionSupplier, optimizeFlowInSearchTree);
        this.random = random;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        this.trainableRewardApproximator = nodeEvaluator.getTrainableApproximator();
    }

    public TrainableApproximator getTrainableRewardApproximator() {
        return trainableRewardApproximator;
    }



    public PaperPolicyImpl initializePolicy(ImmutableStateImpl initialState) {
        return createPolicy(initialState);
    }

    public PaperPolicyImplWithExploration initializePolicyWithExploration(ImmutableStateImpl initialState) {
        return new PaperPolicyImplWithExploration(random, createPolicy(initialState), explorationConstant, temperature);
    }

    public void train(List<ImmutableTuple<DoubleVectorialObservation, double[]>> trainData) {
        trainableRewardApproximator.train(trainData);
    }
}
