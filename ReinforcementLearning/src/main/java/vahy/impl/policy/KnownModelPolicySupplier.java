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
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> extends AbstractRandomizedPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    public KnownModelPolicySupplier(SplittableRandom random) {
        super(random);
    }

    @Override
    protected Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> initializePolicy_inner(TState initialState, PolicyMode policyMode, SplittableRandom random) {
        return new KnownModelPolicy<>(random.split());
    }
}
