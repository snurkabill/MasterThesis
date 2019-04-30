package vahy.paperGenerics.policy.environment;

import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;

import java.util.SplittableRandom;

public class EnvironmentPolicySupplier extends PaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> {

    public final SplittableRandom random;

    public EnvironmentPolicySupplier(SplittableRandom random) {
        super(null, null, 0.0, random, null, null, null, null, null);
        this.random = random;
    }

    @Override
    public PaperPolicy<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> initializePolicy(HallwayStateImpl initialState) {
        return new PaperEnvironmentPolicy(random);
    }
}
