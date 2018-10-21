package vahy.AlphaGo.policy;

import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;

import java.util.SplittableRandom;

public class AlphaGoEnvironmentPolicySupplier {

    public final SplittableRandom random;

    public AlphaGoEnvironmentPolicySupplier(SplittableRandom random) {
        this.random = random;
    }

    public EnvironmentPolicy initializePolicy(ImmutableStateImpl initialState) {
        return new EnvironmentPolicy(random);
    }
}
