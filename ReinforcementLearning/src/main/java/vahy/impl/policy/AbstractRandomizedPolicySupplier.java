package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;

import java.util.SplittableRandom;

public abstract class AbstractRandomizedPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> implements PolicySupplier<TAction, TObservation, TState, TPolicyRecord> {

    private final SplittableRandom random;
    private final int policyId;

    public AbstractRandomizedPolicySupplier(SplittableRandom random, int policyId) {
        this.random = random;
        this.policyId = policyId;
    }

    public int getPolicyId() {
        return policyId;
    }

    @Override
    public Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode) {
        return initializePolicy_inner(initialState, policyMode, random.split());
    }

    protected abstract Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy_inner(TState initialState, PolicyMode policyMode, SplittableRandom random);
}
