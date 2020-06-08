package vahy.impl.policy;

import org.jetbrains.annotations.NotNull;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.Predictor;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.runner.PolicyDefinition;

import java.util.List;
import java.util.function.Supplier;

public class ValuePolicyDefinitionSupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    public PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase> getPolicyDefinition(int policyId, int categoryId, Supplier<Double> explorationConstnatSupplier, PredictorTrainingSetup<TAction, TObservation, TState, PolicyRecordBase> trainablePredictor) {
        return new PolicyDefinition<TAction, TObservation, TState, PolicyRecordBase>(
            policyId,
            categoryId,
            getValuePolicySupplier(explorationConstnatSupplier, (Predictor<TObservation>)trainablePredictor.getTrainablePredictor()),
            List.of(trainablePredictor)
        );
    }

    @NotNull
    private OuterDefPolicySupplier<TAction, TObservation, TState, PolicyRecordBase> getValuePolicySupplier(Supplier<Double> explorationConstantSupplier, Predictor<TObservation> trainablePredictor) {
        return (initialState, policyMode, policyId, random) -> {
            if (policyMode == PolicyMode.INFERENCE) {
                return new ValuePolicy<TAction, TObservation, TState>(random.split(), policyId, trainablePredictor, 0.0);
            }
            return new ValuePolicy<TAction, TObservation, TState>(random.split(), policyId, trainablePredictor, explorationConstantSupplier.get());
        };
    }
}
