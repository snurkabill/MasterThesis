package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;

import java.util.SplittableRandom;

public abstract class RandomizedPolicy<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    protected final SplittableRandom random;

    protected RandomizedPolicy(SplittableRandom random) {
        this.random = random;
    }
}
