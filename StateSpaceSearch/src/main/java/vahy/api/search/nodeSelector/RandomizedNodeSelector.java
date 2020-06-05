package vahy.api.search.nodeSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;

import java.util.SplittableRandom;

public abstract class RandomizedNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    implements NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static Logger logger = LoggerFactory.getLogger(RandomizedNodeSelector.class.getName());
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    protected static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

    protected final SplittableRandom random;

    protected RandomizedNodeSelector(SplittableRandom random) {
        this.random = random;
    }
}
