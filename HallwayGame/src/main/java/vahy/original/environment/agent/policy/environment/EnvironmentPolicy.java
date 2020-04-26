package vahy.original.environment.agent.policy.environment;

import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicy;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.state.HallwayStateImpl;

import java.util.SplittableRandom;

public class EnvironmentPolicy<TPolicyRecord extends PolicyRecord> extends KnownModelPolicy<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, TPolicyRecord> {

    public EnvironmentPolicy(SplittableRandom random) {
        super(random);
    }

}
