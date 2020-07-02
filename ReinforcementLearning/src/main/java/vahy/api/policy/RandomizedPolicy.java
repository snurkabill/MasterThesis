package vahy.api.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public abstract class RandomizedPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord>
    implements Policy<TAction, TObservation, TState, TPolicyRecord> {

    public static final double[] EMPTY_ARRAY = new double[0];

    protected static final Logger logger = LoggerFactory.getLogger(RandomizedPolicy.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

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
