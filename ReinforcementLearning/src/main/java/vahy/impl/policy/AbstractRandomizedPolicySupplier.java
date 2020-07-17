package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
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
    TPolicyRecord extends PolicyRecord> implements PolicySupplier<TAction, TObservation, TState> {

    private final SplittableRandom random;
    private final int policyId;
    private final int policyCategoryId;


    public AbstractRandomizedPolicySupplier(SplittableRandom random, int policyId, int policyCategoryId) {
        this.random = random;
        this.policyId = policyId;
        this.policyCategoryId = policyCategoryId;
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
    public Policy<TAction, TObservation, TState> initializePolicy(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode) {
        return initializePolicy_inner(initialState, policyMode, random.split());
    }

    protected abstract Policy<TAction, TObservation, TState> initializePolicy_inner(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode, SplittableRandom random);
}
