package vahy.impl.benchmark;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.policy.PolicySupplierFactory;
import vahy.impl.learning.trainer.PredictorTrainingSetup;

import java.util.List;
import java.util.SplittableRandom;
import java.util.function.BiFunction;

public class OptimizedPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final int policyId;
    private final int policyCategoryId;
    private final List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList;
    private final PolicySupplierFactory<TAction, TObservation, TState, TPolicyRecord> policySupplierFactory;

    public OptimizedPolicy(int policyId,
                           int policyCategoryId,
                           List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> trainablePredictorSetupList,
                           PolicySupplierFactory<TAction, TObservation, TState, TPolicyRecord> policySupplierFactory) {
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

    public List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> getTrainablePredictorSetupList() {
        return trainablePredictorSetupList;
    }

    public PolicySupplierFactory<TAction, TObservation, TState, TPolicyRecord> getPolicySupplierFactory() {
        return policySupplierFactory;
    }

}
