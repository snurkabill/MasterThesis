package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public abstract class AbstractPolicySupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord>
    implements PolicySupplier<TAction, TObservation, TState, TPolicyRecord> {

    private final int policyId;
    private final int policyCategoryId;
    private final SplittableRandom random;

    protected AbstractPolicySupplier(int policyId, int policyCategoryId, SplittableRandom random) {
        this.policyId = policyId;
        this.policyCategoryId = policyCategoryId;
        this.random = random;
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
    public Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode) {
        return createState_inner(initialState, policyMode, policyId, random.split());
    }

    protected abstract Policy<TAction, TObservation, TState, TPolicyRecord> createState_inner(TState initialState, PolicyMode policyMode, int policyId, SplittableRandom random);

}
