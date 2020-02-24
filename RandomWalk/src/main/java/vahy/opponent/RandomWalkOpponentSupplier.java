package vahy.opponent;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkProbabilities;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.AbstractRandomizedPolicySupplier;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class RandomWalkOpponentSupplier extends AbstractRandomizedPolicySupplier<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState, PaperPolicyRecord> {

    public RandomWalkOpponentSupplier(SplittableRandom random) {
        super(random);
    }

    @Override
    protected Policy<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState, PaperPolicyRecord> initializePolicy_inner(RandomWalkState initialState, PolicyMode policyMode, SplittableRandom random) {
        return new RandomWalkPolicy(random);
    }
}
