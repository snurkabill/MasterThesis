package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;

import java.util.SplittableRandom;

public class KnownModelPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord> extends AbstractRandomizedPolicySupplier<TAction, TObservation, TState, TPolicyRecord> {

    public KnownModelPolicySupplier(SplittableRandom random, int policyId) {
        super(random, policyId);
    }

    @Override
    protected Policy<TAction, TObservation, TState, TPolicyRecord> initializePolicy_inner(TState initialState, PolicyMode policyMode, SplittableRandom random) {
        return new KnownModelPolicy<>(random.split(), this.getPolicyId());
    }
}
