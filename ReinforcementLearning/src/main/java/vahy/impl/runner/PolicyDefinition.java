package vahy.impl.runner;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.PolicySupplierFactory;
import vahy.api.policy.PolicySupplierImpl;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class PolicyDefinition<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    private static final int NO_LOOKBACK_SIZE = 1;

    private final int policyId;
    private final int categoryId;
    private final int observationLookbackSize;
    private final PolicySupplierFactory<TAction, TObservation, TState> policySupplierFactory;
    private final List<PredictorTrainingSetup<TAction, TObservation, TState>> trainablePredictorSetupList;
    private final OuterDefPolicySupplier<TAction, TObservation, TState> outerDefPolicySupplier;

    public PolicyDefinition(int policyId,
                            int categoryId,
                            OuterDefPolicySupplier<TAction, TObservation, TState> outerDefPolicySupplier,
                            List<PredictorTrainingSetup<TAction, TObservation, TState>> trainablePredictorSetupList) {
        this(policyId, categoryId, NO_LOOKBACK_SIZE, outerDefPolicySupplier, trainablePredictorSetupList);
    }

    public PolicyDefinition(int policyId,
                            int categoryId,
                            int observationLookbackSize, OuterDefPolicySupplier<TAction, TObservation, TState> outerDefPolicySupplier,
                            List<PredictorTrainingSetup<TAction, TObservation, TState>> trainablePredictorSetupList) {
        this.policyId = policyId;
        this.categoryId = categoryId;
        this.observationLookbackSize = observationLookbackSize;
        this.policySupplierFactory = (policyId_, categoryId_, random_) -> new PolicySupplierImpl<>(policyId_, categoryId_, this.observationLookbackSize, random_, outerDefPolicySupplier);
        this.trainablePredictorSetupList = trainablePredictorSetupList;
        this.outerDefPolicySupplier = outerDefPolicySupplier;
    }

    public int getPolicyId() {
        return policyId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public PolicySupplierFactory<TAction, TObservation, TState> getPolicySupplierFactory() {
        return policySupplierFactory;
    }

    public List<PredictorTrainingSetup<TAction, TObservation, TState>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    @Override
    public String toString() {
        return "PolicyDefinition{" +
            "policyId=" + policyId +
            ", categoryId=" + categoryId +
            ", OuterDefPolicySupplier=" + outerDefPolicySupplier +
            ", trainablePredictorSetupList=" + trainablePredictorSetupList +
            '}';
    }
}
