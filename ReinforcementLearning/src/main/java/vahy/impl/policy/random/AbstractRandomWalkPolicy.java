package vahy.impl.policy.random;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;

import java.util.SplittableRandom;

public abstract class AbstractRandomWalkPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements Policy<TAction, TReward, TObservation, TState> {

    private final SplittableRandom random;

    public AbstractRandomWalkPolicy(SplittableRandom random) {
        this.random = random;
    }

    protected SplittableRandom getRandom() {
        return random;
    }
}
