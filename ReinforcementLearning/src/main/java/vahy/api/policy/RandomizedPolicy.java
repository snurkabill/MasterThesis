package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public abstract class RandomizedPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord>
    implements Policy<TAction, TObservation, TState, TPolicyRecord> {

    protected static final double[] EMPTY_ARRAY = new double[0];

    protected final SplittableRandom random;
    protected final int policyId;

    protected RandomizedPolicy(SplittableRandom random, int policyId) {
        this.random = random;
        this.policyId = policyId;
    }

    @Override
    public int getPolicyId() {
        return policyId;
    }

}
