package vahy.original.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.AbstractRandomizedPolicySupplier;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class HallwayPolicySupplier extends AbstractRandomizedPolicySupplier<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, PaperPolicyRecord> {

    public HallwayPolicySupplier(SplittableRandom random) {
        super(random);
    }

    @Override
    protected Policy<HallwayAction, DoubleVector, HallwayStateImpl, HallwayStateImpl, PaperPolicyRecord> initializePolicy_inner(HallwayStateImpl initialState, PolicyMode policyMode, SplittableRandom random) {
        return new PaperEnvironmentPolicy(random);
    }
}
