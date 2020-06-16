package vahy.impl.search.AlphaZero;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.PolicyRecordBase;
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

public class AlphaZeroPolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TObservation extends DoubleVector, TState extends State<TAction, TObservation, TState>> {

    private final Class<TAction> actionClass;
    private final int enumConstantsLength;
    private final SearchNodeFactory<TAction, TObservation, AlphaZeroNodeMetadata<TAction>, TState> searchNodeFactory;
    private final AlphaZeroNodeMetadataFactory<TAction, TObservation, TState> metadataFactory;

    public AlphaZeroPolicyDefinitionSupplier(Class<TAction> actionClass, int inGameEntityCount) {
        this.actionClass = actionClass;
        this.enumConstantsLength = actionClass.getEnumConstants().length;
        this.metadataFactory = new AlphaZeroNodeMetadataFactory<>(actionClass, inGameEntityCount);
        this.searchNodeFactory = new SearchNodeBaseFactoryImpl<>(actionClass, metadataFactory);
    }


    public PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinition(int policyId, int categoryId, double cpuctParameter, Supplier<Double> exploration, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, TObservation, TState, PolicyRecordBase> predictorSetup) {
        return new PolicyDefinition<>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithPredictor(cpuctParameter, exploration, treeExpansionCountPerStep, predictorSetup.getTrainablePredictor()),
            List.of(predictorSetup)
        );
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinitionSupplierWithPredictor(double cpuctParameter, Supplier<Double> exploration, int treeExpansionCountPerStep, TrainablePredictor predictor)
    {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            var searchTree = new SearchTreeImpl<TAction, TObservation, AlphaZeroNodeMetadata<TAction>, TState>(
                searchNodeFactory, root,
                new AlphaZeroNodeSelector<>(random_, cpuctParameter, enumConstantsLength),
                new AlphaZeroTreeUpdater<>(),
                new AlphaZeroEvaluator<>(searchNodeFactory, predictor)
            );
            var searchExpansionCondition = new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep);
            switch (policyMode_) {
                case INFERENCE:
                    return new AlphaZeroPolicy<>(policyId_, random_, 0.0, searchExpansionCondition, searchTree);
                case TRAINING:
                    return new AlphaZeroPolicy<TAction, TObservation, TState>(policyId_, random_, exploration.get(), searchExpansionCondition, searchTree);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(policyMode_);
            }
        };
    }


}
