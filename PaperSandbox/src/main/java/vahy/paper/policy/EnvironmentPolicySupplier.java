package vahy.paper.policy;

import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;

import java.util.SplittableRandom;

public class EnvironmentPolicySupplier {

    public final SplittableRandom random;

    public EnvironmentPolicySupplier(SplittableRandom random) {
        this.random = random;
    }

    public EnvironmentPolicy initializePolicy(ImmutableStateImpl initialState) {
        return new EnvironmentPolicy(random);
    }
}
