package vahy.paperOldImpl.policy;

import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.paperOldImpl.tree.nodeEvaluator.ApproximatorBasedNodeEvaluator;
import vahy.paperOldImpl.tree.nodeExpander.NodeExpander;
import vahy.paperOldImpl.tree.treeUpdater.TreeUpdater;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class PaperTrainablePaperPolicySupplier extends PaperPolicySupplier {

    private final SplittableRandom random;
    private final double explorationConstant;
    private final double temperature;

    private TrainableApproximator trainableRewardApproximator;

    public PaperTrainablePaperPolicySupplier(SplittableRandom random,
                                             double explorationConstant,
                                             double temperature,
                                             double totalRiskAllowed,
                                             ApproximatorBasedNodeEvaluator nodeEvaluator,
                                             NodeExpander nodeExpander,
                                             TreeUpdater treeUpdater,
                                             TreeUpdateConditionFactory treeUpdateConditionFactory,
                                             double cpuctParameter,
                                             boolean optimizeFlowInSearchTree) {
        super(cpuctParameter, totalRiskAllowed, random, nodeEvaluator, nodeExpander, treeUpdater, treeUpdateConditionFactory, optimizeFlowInSearchTree);
        this.random = random;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        this.trainableRewardApproximator = nodeEvaluator.getTrainableApproximator();
    }

    public TrainableApproximator getTrainableRewardApproximator() {
        return trainableRewardApproximator;
    }



    public PaperPolicyImpl initializePolicy(HallwayStateImpl initialState) {
        return createPolicy(initialState);
    }

    public PaperPolicyImplWithExploration initializePolicyWithExploration(HallwayStateImpl initialState) {
        return new PaperPolicyImplWithExploration(random, createPolicy(initialState), explorationConstant, temperature);
    }

    public void train(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        trainableRewardApproximator.train(trainData);
    }
}
