package vahy.impl.runner;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class PolicyArguments<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    private final int policyId;
    private final String policyName;
    private final PolicySupplier<TAction, TObservation, TState, TPolicyRecord> policySupplier;
    private final List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList;

    public PolicyArguments(int policyId,
                           String policyName,
                           PolicySupplier<TAction, TObservation, TState, TPolicyRecord> policySupplier,
                           List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.policySupplier = policySupplier;
        this.trainablePredictorSetupList = trainablePredictorSetupList;
    }

    public int getPolicyId() {
        return policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public PolicySupplier<TAction, TObservation, TState, TPolicyRecord> getPolicySupplier() {
        return policySupplier;
    }

    public List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }
}
