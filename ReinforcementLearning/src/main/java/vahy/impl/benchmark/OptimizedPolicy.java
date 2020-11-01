package vahy.impl.benchmark;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicySupplierFactory;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;

public class OptimizedPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>> {

    private final int policyId;
    private final int policyCategoryId;
    private final List<PredictorTrainingSetup<TAction, TObservation, TState>> trainablePredictorSetupList;
    private final PolicySupplierFactory<TAction, TObservation, TState> policySupplierFactory;

    public OptimizedPolicy(int policyId,
                           int policyCategoryId,
                           List<PredictorTrainingSetup<TAction, TObservation, TState>> trainablePredictorSetupList,
                           PolicySupplierFactory<TAction, TObservation, TState> policySupplierFactory) {
        this.policyId = policyId;
        this.policyCategoryId = policyCategoryId;
        this.trainablePredictorSetupList = trainablePredictorSetupList;
        this.policySupplierFactory = policySupplierFactory;
    }

    public int getPolicyId() {
        return policyId;
    }

    public int getPolicyCategoryId() {
        return policyCategoryId;
    }

    public List<PredictorTrainingSetup<TAction, TObservation, TState>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    public PolicySupplierFactory<TAction, TObservation, TState> getPolicySupplierFactory() {
        return policySupplierFactory;
    }

}
