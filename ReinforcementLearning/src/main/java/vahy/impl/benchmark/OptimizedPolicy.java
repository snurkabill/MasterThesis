package vahy.impl.benchmark;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class OptimizedPolicy<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final String policyId;
    private final List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainablePredictorSetupList;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier;

    public OptimizedPolicy(String policyId, List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> trainablePredictorSetupList, PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier) {
        this.policyId = policyId;
        this.trainablePredictorSetupList = trainablePredictorSetupList;
        this.policySupplier = policySupplier;
    }

    public String getPolicyId() {
        return policyId;
    }

    public List<PredictorTrainingSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicySupplier() {
        return policySupplier;
    }

}
