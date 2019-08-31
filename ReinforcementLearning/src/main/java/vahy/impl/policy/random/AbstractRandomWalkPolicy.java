package vahy.impl.policy.random;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

import java.util.SplittableRandom;

public abstract class AbstractRandomWalkPolicy<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements Policy<TAction, TPlayerObservation, TOpponentObservation, TState> {

    private final SplittableRandom random;

    public AbstractRandomWalkPolicy(SplittableRandom random) {
        this.random = random;
    }

    protected SplittableRandom getRandom() {
        return random;
    }
}
