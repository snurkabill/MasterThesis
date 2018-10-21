package vahy.paper.policy;

import vahy.paper.reinforcement.TrainableApproximator;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class PaperTrainablePolicySupplier extends PolicySupplier {

    private final SplittableRandom random;
    private final double explorationConstant;
    private final double temperature;

    private TrainableApproximator trainableRewardApproximator;

    public PaperTrainablePolicySupplier(SplittableRandom random, double explorationConstant, double temperature, double totalRiskAllowed, TrainableApproximator trainableRewardApproximator, double cpuctParameter, int treeUpdateCount, boolean optimizeFlowInSearchTree) {
        super(cpuctParameter, treeUpdateCount, totalRiskAllowed, random, trainableRewardApproximator, optimizeFlowInSearchTree);
        this.random = random;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        this.trainableRewardApproximator = trainableRewardApproximator;
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
