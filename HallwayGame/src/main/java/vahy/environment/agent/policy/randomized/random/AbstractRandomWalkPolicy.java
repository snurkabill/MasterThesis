package vahy.environment.agent.policy.randomized.random;

import vahy.environment.agent.policy.IPolicy;

import java.util.SplittableRandom;

public abstract class AbstractRandomWalkPolicy implements IPolicy {

    private final SplittableRandom random;

    public AbstractRandomWalkPolicy(SplittableRandom random) {
        this.random = random;
    }

    protected SplittableRandom getRandom() {
        return random;
    }
}
