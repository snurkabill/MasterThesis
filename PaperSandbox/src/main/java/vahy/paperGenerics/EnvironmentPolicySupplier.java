package vahy.paperGenerics;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;

import java.util.SplittableRandom;

public class EnvironmentPolicySupplier implements PolicySupplier<ActionType, DoubleReward, DoubleVectorialObservation, ImmutableStateImpl> {

    public final SplittableRandom random;

    public EnvironmentPolicySupplier(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public Policy<ActionType, DoubleReward, DoubleVectorialObservation, ImmutableStateImpl> initializePolicy(ImmutableStateImpl initialState) {
        return new EnvironmentPolicy(random);
    }
}
