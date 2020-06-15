package vahy.impl.policy;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.Predictor;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;

import java.util.List;
import java.util.function.Supplier;

public class ValuePolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> {

    public PolicyDefinition<TAction, DoubleVector, TState, PolicyRecordBase> getPolicyDefinition(int policyId, int categoryId, Supplier<Double> explorationConstnatSupplier, PredictorTrainingSetup<TAction, DoubleVector, TState, PolicyRecordBase> trainablePredictor) {
        return new PolicyDefinition<TAction, DoubleVector, TState, PolicyRecordBase>(
            policyId,
            categoryId,
            getValuePolicySupplier(explorationConstnatSupplier, (Predictor<DoubleVector>)trainablePredictor.getTrainablePredictor()),
            List.of(trainablePredictor)
        );
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, DoubleVector, TState, PolicyRecordBase> getValuePolicySupplier(Supplier<Double> explorationConstantSupplier, Predictor<DoubleVector> trainablePredictor) {
        return (initialState, policyMode, policyId, random) -> {
            if (policyMode == PolicyMode.INFERENCE) {
                return new ValuePolicy<TAction, TState>(random.split(), policyId, trainablePredictor, 0.0);
            }
            return new ValuePolicy<TAction, TState>(random.split(), policyId, trainablePredictor, explorationConstantSupplier.get());
        };
    }
}
