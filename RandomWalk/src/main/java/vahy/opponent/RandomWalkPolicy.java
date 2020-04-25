package vahy.opponent;

import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicy;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class RandomWalkPolicy extends KnownModelPolicy<RandomWalkAction, DoubleVector, RandomWalkState, RandomWalkState, PaperPolicyRecord> {

    public RandomWalkPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public PaperPolicyRecord getPolicyRecord(RandomWalkState gameState) {
        return new PaperPolicyRecord(null, null, 0, 0, 0, 0);
    }
}
