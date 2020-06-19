package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public class PolicySupplierImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord>
    implements PolicySupplier<TAction, TObservation, TState, TPolicyRecord> {

    private final int policyId;
    private final int policyCategoryId;
    private final SplittableRandom random;
    private final OuterDefPolicySupplier<TAction, TObservation, TState, TPolicyRecord> outerDefPolicySupplier;

    public PolicySupplierImpl(int policyId, int policyCategoryId, SplittableRandom random, OuterDefPolicySupplier<TAction, TObservation, TState, TPolicyRecord> outerDefPolicySupplier) {
        this.policyId = policyId;
        this.policyCategoryId = policyCategoryId;
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
    public Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode) {
        return outerDefPolicySupplier.apply(initialState, policyMode, policyId, random.split());
    }

    @Override
    public String toString() {
        return "PolicySupplierImpl{" +
            "policyId=" + policyId +
            ", policyCategoryId=" + policyCategoryId +
            ", random=" + random +
            ", outerDefPolicySupplier=" + outerDefPolicySupplier +
            '}';
    }
}
