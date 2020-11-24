package vahy.examples.patrolling;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.SplittableRandom;

public class PatrollingInitializer extends AbstractInitialStateSupplier<PatrollingConfig, PatrollingAction, DoubleVector, PatrollingState> {


    private final PatrollingStaticPart staticPart;

    protected PatrollingInitializer(PatrollingConfig problemConfig, SplittableRandom random) {
        super(problemConfig, random);
        this.staticPart = new PatrollingStaticPart(problemConfig.getGraph());
    }

    @Override
    protected PatrollingState createState_inner(PatrollingConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        return new PatrollingState(staticPart, random.nextInt(staticPart.getNodeCount()));
    }
}
