package vahy.environment.agent.policy.random;

import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.agent.policy.IPolicy;

import java.util.Random;

public abstract class AbstarctRandomWalkPolicy implements IPolicy, IOneHotPolicy {

    private final Random random;

    public AbstarctRandomWalkPolicy(Random random) {
        this.random = random;
    }

    protected Random getRandom() {
        return random;
    }
}
