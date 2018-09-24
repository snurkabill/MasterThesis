package vahy.AlphaGo.policy;

import vahy.AlphaGo.reinforcement.AlphaGoTrainableApproximator;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class AlphaGoTrainablePolicySupplier extends AlphaGoPolicySupplier {

    private final SplittableRandom random;
    private final double explorationConstant;

    private AlphaGoTrainableApproximator trainableRewardApproximator;

    public AlphaGoTrainablePolicySupplier(SplittableRandom random, double explorationConstant, double discountFactor, AlphaGoTrainableApproximator trainableRewardApproximator, double cpuctParameter, int treeUpdateCount) {
        super(cpuctParameter, treeUpdateCount, discountFactor, random, trainableRewardApproximator);
        this.random = random;
        this.explorationConstant = explorationConstant;
        this.trainableRewardApproximator = trainableRewardApproximator;
    }

    public AlphaGoTrainableApproximator getTrainableRewardApproximator() {
        return trainableRewardApproximator;
    }



    public AlphaGoPolicyImpl initializePolicy(ImmutableStateImpl initialState) {
        return createPolicy(initialState);
    }

    public AlphaGoPolicyImplWithExploration initializePolicyWithExploration(ImmutableStateImpl initialState) {
        return new AlphaGoPolicyImplWithExploration(random, createPolicy(initialState), explorationConstant);
    }

    public void train(List<ImmutableTuple<DoubleVectorialObservation, double[]>> trainData) {
        trainableRewardApproximator.train(trainData);
    }
}
