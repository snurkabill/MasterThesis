package vahy.impl.policy.mcts;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateConditionSuplierCountBased;
import vahy.utils.EnumUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class MCTSPolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> {

    private final Class<TAction> actionClass;
    private final int enumConstantsLength;
    private final SearchNodeFactory<TAction, DoubleVector, MCTSMetadata, TState> searchNodeFactory;
    private final MCTSMetadataFactory<TAction, DoubleVector, TState> metadataFactory;
    private final boolean isModelKnown;

    public MCTSPolicyDefinitionSupplier(Class<TAction> actionClass, int inGameEntityCount, boolean isModelKnown) {
        this.actionClass = actionClass;
        this.enumConstantsLength = actionClass.getEnumConstants().length;
        this.metadataFactory = new MCTSMetadataFactory<>(inGameEntityCount);
        this.searchNodeFactory = new SearchNodeBaseFactoryImpl<>(actionClass, metadataFactory);
        this.isModelKnown = isModelKnown;
    }

    public PolicyDefinition<TAction, DoubleVector, TState> getPolicyDefinition(int policyId, int categoryId, double cpuctParameter, int treeExpansionCountPerStep, double discountFactor, int rolloutCount) {
        return new PolicyDefinition<TAction, DoubleVector, TState>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithRollout(cpuctParameter, treeExpansionCountPerStep, discountFactor, rolloutCount),
            new ArrayList<>(0)
        );
    }

    public PolicyDefinition<TAction, DoubleVector, TState> getPolicyDefinition(int policyId, int categoryId, Supplier<Double> explorationConstantSupplier, double cpuctParameter, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, DoubleVector, TState> predictorSetup, int maximalEvaluationDepth) {
        return new PolicyDefinition<>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithPredictor(cpuctParameter, explorationConstantSupplier, treeExpansionCountPerStep, predictorSetup.getTrainablePredictor(), maximalEvaluationDepth),
            List.of(predictorSetup)
        );
    }

    public PolicyDefinition<TAction, DoubleVector, TState> getPolicyDefinition(int policyId, int categoryId, Supplier<Double> explorationConstantSupplier, double cpuctParameter, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, DoubleVector, TState> predictorSetup) {
        return getPolicyDefinition(policyId, categoryId, explorationConstantSupplier, cpuctParameter, treeExpansionCountPerStep, predictorSetup, 0);
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, DoubleVector, TState> getPolicyDefinitionSupplierWithRollout(double cpuctParameter,
                                                                                                         int treeExpansionCountPerStep,
                                                                                                         double discountFactor,
                                                                                                         int rolloutCount) {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            return new MCTSPolicy<TAction, DoubleVector, TState>(policyId_, random_, 0.0, new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep),
                new SearchTreeImpl<>(
                    searchNodeFactory, root,
                    new Ucb1NodeSelector<>(random_, isModelKnown, cpuctParameter, enumConstantsLength),
                    new MCTSTreeUpdater<>(),
                    new MCTSRolloutEvaluator<>(searchNodeFactory, random_, discountFactor, rolloutCount)
                ));
        };
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, DoubleVector, TState> getPolicyDefinitionSupplierWithPredictor(double cpuctParameter,
                                                                                                           Supplier<Double> explorationConstantSupplier,
                                                                                                           int treeExpansionCountPerStep,
                                                                                                           TrainablePredictor predictor,
                                                                                                           int maximalEvaluationDepth) {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            var searchTree = new SearchTreeImpl<>(
                searchNodeFactory, root,
                new Ucb1NodeSelector<>(random_, isModelKnown, cpuctParameter, enumConstantsLength),
//                new UnfairUcb1NodeSelector<>(random_, cpuctParameter, enumConstantsLength),
                new MCTSTreeUpdater<>(),
                maximalEvaluationDepth == 0 ? new MCTSPredictionEvaluator<>(searchNodeFactory, predictor) : new MCTSBatchedEvaluator<>(searchNodeFactory, predictor, maximalEvaluationDepth)
            );
            var treeUpdaterCondition = new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep);
            switch (policyMode_) {
                case INFERENCE:
                    return new MCTSPolicy<TAction, DoubleVector, TState>(policyId_, random_, 0.0, treeUpdaterCondition, searchTree);
                case TRAINING:
                    return new MCTSPolicy<TAction, DoubleVector, TState>(policyId_, random_, explorationConstantSupplier.get(), treeUpdaterCondition, searchTree);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(policyMode_);
            }
        };
    }
}
