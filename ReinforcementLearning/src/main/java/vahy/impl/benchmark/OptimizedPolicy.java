package vahy.impl.benchmark;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;

public class OptimizedPolicy<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final String policyId;
    private final TrainablePredictor trainablePredictor;
    private final PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier;

    public OptimizedPolicy(String policyId, TrainablePredictor trainablePredictor, PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policySupplier) {
        this.policyId = policyId;
        this.trainablePredictor = trainablePredictor;
        this.policySupplier = policySupplier;
    }

    public String getPolicyId() {
        return policyId;
    }

    public TrainablePredictor getTrainablePredictor() {
        return trainablePredictor;
    }

    public PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPolicySupplier() {
        return policySupplier;
    }

}
