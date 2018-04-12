package vahy.environment.agent.policy.exhaustive;

import vahy.impl.search.nodeSelector.exhaustive.BfsNodeSelector;

import java.util.SplittableRandom;

public class BfsPolicy extends ExhaustivePolicy {

    public BfsPolicy(
        SplittableRandom random,
        double discountFactor,
        int uprateTreeCount) {
        super(random, discountFactor, uprateTreeCount, BfsNodeSelector::new);
    }
}
