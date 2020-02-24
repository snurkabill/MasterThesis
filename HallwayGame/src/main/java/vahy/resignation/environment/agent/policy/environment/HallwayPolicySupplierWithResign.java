package vahy.resignation.environment.agent.policy.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.AbstractRandomizedPolicySupplier;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.state.EnvironmentProbabilities;
import vahy.resignation.environment.state.HallwayStateWithResign;

import java.util.SplittableRandom;

public class HallwayPolicySupplierWithResign extends AbstractRandomizedPolicySupplier<HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign, PaperPolicyRecord> {

    public HallwayPolicySupplierWithResign(SplittableRandom random) {
        super(random);
    }

    @Override
    protected Policy<HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign, PaperPolicyRecord> initializePolicy_inner(HallwayStateWithResign initialState, PolicyMode policyMode, SplittableRandom random) {
        return new PaperEnvironmentPolicyWithResign(random);
    }


}
