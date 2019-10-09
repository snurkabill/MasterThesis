package vahy.opponent;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkProbabilities;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;

import java.util.SplittableRandom;

public class RandomWalkOpponentSupplier extends PaperPolicySupplier<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> {

    public final SplittableRandom random;

    public RandomWalkOpponentSupplier(SplittableRandom random) {
        super(null, null, 0.0, random, null, null, null, null, null, null, null, null);
        this.random = random;
    }

    @Override
    public Policy<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState, PaperPolicyRecord> initializePolicy(RandomWalkState initialState, PolicyMode policyMode) {
        return new RandomWalkPolicy(random.split());
    }
}
