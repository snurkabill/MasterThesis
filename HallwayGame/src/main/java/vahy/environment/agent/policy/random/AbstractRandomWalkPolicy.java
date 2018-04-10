package vahy.environment.agent.policy.random;

import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.agent.policy.IPolicy;

import java.util.SplittableRandom;

public abstract class AbstractRandomWalkPolicy implements IPolicy, IOneHotPolicy {

    private final SplittableRandom random;

    public AbstractRandomWalkPolicy(SplittableRandom random) {
        this.random = random;
    }

    protected SplittableRandom getRandom() {
        return random;
    }
}
