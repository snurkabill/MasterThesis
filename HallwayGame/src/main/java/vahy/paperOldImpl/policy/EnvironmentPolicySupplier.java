package vahy.paperOldImpl.policy;

import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.HallwayStateImpl;

import java.util.SplittableRandom;

public class EnvironmentPolicySupplier {

    public final SplittableRandom random;

    public EnvironmentPolicySupplier(SplittableRandom random) {
        this.random = random;
    }

    public EnvironmentPolicy initializePolicy(HallwayStateImpl initialState) {
        return new EnvironmentPolicy(random);
    }
}
