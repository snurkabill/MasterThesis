package vahy.impl.policy.alphazero;

import vahy.api.experiment.ProblemConfig;
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

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class AlphaZeroPolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> {

    private final Class<TAction> actionClass;
    private final int enumConstantsLength;
    private final SearchNodeFactory<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> searchNodeFactory;
    private final AlphaZeroNodeMetadataFactory<TAction, DoubleVector, TState> metadataFactory;
    private final boolean isModelKnown;

    public AlphaZeroPolicyDefinitionSupplier(Class<TAction> actionClass, int inGameEntityCount, ProblemConfig problemConfig) {
        this.actionClass = actionClass;
        this.enumConstantsLength = actionClass.getEnumConstants().length;
        this.metadataFactory = new AlphaZeroNodeMetadataFactory<>(actionClass, inGameEntityCount);
        this.searchNodeFactory = new SearchNodeBaseFactoryImpl<>(actionClass, metadataFactory);
        this.isModelKnown = problemConfig.isModelKnown();
    }

    public PolicyDefinition<TAction, DoubleVector, TState> getPolicyDefinition(int policyId, int categoryId, double cpuctParameter, Supplier<Double> exploration, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, DoubleVector, TState> predictorSetup, int maxDepthEvaluations) {
        return new PolicyDefinition<>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithPredictor(cpuctParameter, exploration, treeExpansionCountPerStep, predictorSetup.getTrainablePredictor(), maxDepthEvaluations),
            List.of(predictorSetup)
        );
    }

    public PolicyDefinition<TAction, DoubleVector, TState> getPolicyDefinition(int policyId, int categoryId, double cpuctParameter, Supplier<Double> exploration, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, DoubleVector, TState> predictorSetup) {
        return getPolicyDefinition(policyId, categoryId, cpuctParameter, exploration, treeExpansionCountPerStep, predictorSetup, 0);
    }

    private OuterDefPolicySupplier<TAction, DoubleVector, TState> getPolicyDefinitionSupplierWithPredictor(double cpuctParameter, Supplier<Double> exploration, int treeExpansionCountPerStep, TrainablePredictor predictor, int maximalDepthEvaluation)
    {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            var searchTree = new SearchTreeImpl<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState>(
                searchNodeFactory,
                root,
                new AlphaZeroNodeSelector<>(random_, isModelKnown, cpuctParameter, enumConstantsLength),
                new AlphaZeroTreeUpdater<>(),
                maximalDepthEvaluation == 0 ?
                    new AlphaZeroEvaluator<>(searchNodeFactory, predictor, isModelKnown) :
                    new AlphaZeroBatchedEvaluator<TAction, TState>(searchNodeFactory, predictor, maximalDepthEvaluation, isModelKnown)
            );
            var searchExpansionCondition = new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep);
            switch (policyMode_) {
                case INFERENCE:
                    return new AlphaZeroPolicy<>(policyId_, random_, 0.0, searchExpansionCondition, searchTree);
                case TRAINING:
                    return new AlphaZeroPolicy<TAction, DoubleVector, TState>(policyId_, random_, exploration.get(), searchExpansionCondition, searchTree);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(policyMode_);
            }
        };
    }


}
