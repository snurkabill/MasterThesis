package vahy.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;

import java.util.SplittableRandom;

public class EnvironmentPolicySupplier extends PaperPolicySupplier<HallwayAction,  DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> {

    public final SplittableRandom random;

    public EnvironmentPolicySupplier(SplittableRandom random) {
        super(null, null, 0.0, random, null, null, null, null, null, null, null, null);
        this.random = random;
    }

//    @Override
//    protected PaperPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> createPolicy(HallwayStateImpl initialState) {
//        return new PaperEnvironmentPolicy(random.split());
//    }

    @Override
    public Policy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord> initializePolicy(HallwayStateImpl initialState, PolicyMode policyMode) {
        return new PaperEnvironmentPolicy(random.split());
    }
}
