package vahy.impl.search.MCTS;

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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class MCTSPolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TObservation extends  DoubleVector, TState extends State<TAction, TObservation, TState>> {


    private final Class<TAction> actionClass;
    private final int enumConstantsLength;
    private final SearchNodeFactory<TAction, TObservation, MCTSMetadata, TState> searchNodeFactory;
    private final MCTSMetadataFactory<TAction, TObservation, TState> metadataFactory;

    public MCTSPolicyDefinitionSupplier(Class<TAction> actionClass, int inGameEntityCount) {
        this.actionClass = actionClass;
        this.enumConstantsLength = actionClass.getEnumConstants().length;
        this.metadataFactory = new MCTSMetadataFactory<>(inGameEntityCount);
        this.searchNodeFactory = new SearchNodeBaseFactoryImpl<>(actionClass, metadataFactory);
    }

    public PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinition(int policyId, int categoryId, double cpuctParameter, int treeExpansionCountPerStep, double discountFactor, int rolloutCount) {
        return new PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithRollout(cpuctParameter,treeExpansionCountPerStep, discountFactor, rolloutCount),
            new ArrayList<>(0)
        );
    }

    public PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinition(int policyId, int categoryId, Supplier<Double> explorationConstantSupplier, double cpuctParameter, int treeExpansionCountPerStep, PredictorTrainingSetup<TAction, TObservation, TState, PolicyRecordBase> predictorSetup) {
        return new PolicyDefinition<>(
            policyId,
            categoryId,
            getPolicyDefinitionSupplierWithPredictor(cpuctParameter, explorationConstantSupplier, treeExpansionCountPerStep, predictorSetup.getTrainablePredictor()),
            List.of(predictorSetup)
        );
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinitionSupplierWithRollout(double cpuctParameter,
                                                                                                                           int treeExpansionCountPerStep,
                                                                                                                           double discountFactor,
                                                                                                                           int rolloutCount)
    {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            return new MCTSPolicy<TAction, TObservation, TState>(policyId_, random_, 0.0, new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep),
                new SearchTreeImpl<>(
                    searchNodeFactory, root,
                    new Ucb1NodeSelector<>(random_, cpuctParameter, enumConstantsLength),
                    new MCTSTreeUpdater<>(),
                    new MCTSRolloutEvaluator<>(searchNodeFactory, random_, discountFactor, rolloutCount)
                ));
        };
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinitionSupplierWithPredictor(double cpuctParameter,
                                                                                                                             Supplier<Double> explorationConstantSupplier,
                                                                                                                             int treeExpansionCountPerStep,
                                                                                                                             TrainablePredictor predictor)
    {
        return (initialState_, policyMode_, policyId_, random_) -> {
            var root = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
            var searchTree = new SearchTreeImpl<>(
                searchNodeFactory, root,
                new Ucb1NodeSelector<>(random_, cpuctParameter, enumConstantsLength),
                new MCTSTreeUpdater<>(),
                new MCTSPredictionEvaluator<>(searchNodeFactory, predictor)
            );
            var treeUpdaterCondition = new TreeUpdateConditionSuplierCountBased(treeExpansionCountPerStep);
            switch (policyMode_) {
                case INFERENCE:
                    return new MCTSPolicy<TAction, TObservation, TState>(policyId_, random_, 0.0, treeUpdaterCondition, searchTree);
                case TRAINING:
                    return new MCTSPolicy<TAction, TObservation, TState>(policyId_, random_, explorationConstantSupplier.get(), treeUpdaterCondition, searchTree);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(policyMode_);
            }
        };
    }
}
