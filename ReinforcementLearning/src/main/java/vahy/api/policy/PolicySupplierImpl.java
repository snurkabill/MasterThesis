package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public class PolicySupplierImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>>
    implements PolicySupplier<TAction, TObservation, TState> {

    private final int policyId;
    private final int policyCategoryId;
    private final int observationLookbackSize;
    private final SplittableRandom random;
    private final OuterDefPolicySupplier<TAction, TObservation, TState> outerDefPolicySupplier;

    public PolicySupplierImpl(int policyId, int policyCategoryId, int observationLookbackSize, SplittableRandom random, OuterDefPolicySupplier<TAction, TObservation, TState> outerDefPolicySupplier) {
        this.policyId = policyId;
        this.policyCategoryId = policyCategoryId;
        this.observationLookbackSize = observationLookbackSize;
        this.random = random;
        this.outerDefPolicySupplier = outerDefPolicySupplier;
    }

    @Override
    public int getPolicyId() {
        return policyId;
    }

    @Override
    public int getPolicyCategoryId() {
        return policyCategoryId;
    }

    @Override
    public int getObservationLookbackSize() {
        return observationLookbackSize;
    }

    @Override
    public Policy<TAction, TObservation, TState> initializePolicy(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode) {
        return outerDefPolicySupplier.apply(initialState, policyMode, policyId, random.split());
    }

    @Override
    public String toString() {
        return "PolicySupplierImpl{" +
            "policyId=" + policyId +
            ", policyCategoryId=" + policyCategoryId +
            ", outerDefPolicySupplier=" + outerDefPolicySupplier +
            '}';
    }
}
