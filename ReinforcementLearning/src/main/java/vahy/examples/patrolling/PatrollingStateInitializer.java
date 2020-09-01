package vahy.examples.patrolling;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.SplittableRandom;

public class PatrollingStateInitializer extends AbstractInitialStateSupplier<PatrollingConfig, PatrollingAction, DoubleVector, PatrollingState> {

    protected PatrollingStateInitializer(PatrollingConfig problemConfig, SplittableRandom random) {
        super(problemConfig, random);
    }

    @Override
    protected PatrollingState createState_inner(PatrollingConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {


        return null;
    }
}
