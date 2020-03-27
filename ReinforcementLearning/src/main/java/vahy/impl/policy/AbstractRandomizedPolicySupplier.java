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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> implements PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private final SplittableRandom random;

    public AbstractRandomizedPolicySupplier(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode) {
        return initializePolicy_inner(initialState, policyMode, random.split());
    }

    protected abstract Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> initializePolicy_inner(TState initialState, PolicyMode policyMode, SplittableRandom random);
}
