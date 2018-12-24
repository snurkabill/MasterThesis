package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;

public class TrainablePaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>>
    extends PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private TrainableNodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> trainableNodeEvaluator;
    private final double explorationConstant;
    private final double temperature;

    public TrainablePaperPolicySupplier(Class<TAction> actionClass,
                                        SearchNodeMetadataFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                                        double totalRiskAllowed,
                                        SplittableRandom random,
                                        NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                                        TrainableNodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                        TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                                        TreeUpdateConditionFactory treeUpdateConditionFactory,
                                        double explorationConstant,
                                        double temperature) {
        super(actionClass, searchNodeMetadataFactory, totalRiskAllowed, random, nodeSelector, nodeEvaluator, treeUpdater, treeUpdateConditionFactory);
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        this.trainableNodeEvaluator = nodeEvaluator;
    }

    public PaperPolicy<TAction, TReward, TObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    public PaperPolicyImplWithExploration<TAction, TReward, TObservation, TSearchNodeMetadata, TState> initializePolicyWithExploration(TState initialState) {
        return new PaperPolicyImplWithExploration<>(getActionClass(), getRandom(), createPolicy(initialState), explorationConstant, temperature);
    }

    public void train(List<ImmutableTuple<TObservation, double[]>> trainData) {
        trainableNodeEvaluator.train(trainData);
    }
}
