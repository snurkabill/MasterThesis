package vahy.impl.runner;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplierFactory;
import vahy.api.policy.PolicySupplierImpl;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class PolicyDefinition<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    private final int policyId;
    private final int categoryId;
    private final PolicySupplierFactory<TAction, TObservation, TState, TPolicyRecord> policySupplierFactory;
    private final List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList;

    public PolicyDefinition(int policyId,
                            int categoryId,
                            OuterDefPolicySupplier<TAction, TObservation, TState, TPolicyRecord> outerDefPolicySupplier,
                            List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList) {
        this.policyId = policyId;
        this.categoryId = categoryId;
        this.policySupplierFactory = (policyId_, categoryId_, random_) -> new PolicySupplierImpl<>(policyId_, categoryId_, random_, outerDefPolicySupplier);
        this.trainablePredictorSetupList = trainablePredictorSetupList;
    }

    public int getPolicyId() {
        return policyId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public PolicySupplierFactory<TAction, TObservation, TState, TPolicyRecord> getPolicySupplierFactory() {
        return policySupplierFactory;
    }

    public List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }
}
