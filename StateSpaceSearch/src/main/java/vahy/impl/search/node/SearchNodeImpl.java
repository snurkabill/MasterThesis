package vahy.impl.search.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.AbstractSearchNode;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SearchNodeImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TSearchNodeMetadata extends NodeMetadata, TState extends State<TAction, TObservation, TState>>
    extends AbstractSearchNode<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(SearchNodeImpl.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

    public static final AtomicLong nodeInstanceId = new AtomicLong(0);
    public final long nodeId;

    private final Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap;

    public SearchNodeImpl(StateWrapper<TAction, TObservation, TState> wrappedState, TSearchNodeMetadata searchNodeMetadata, Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap) {
        this(wrappedState, searchNodeMetadata, childNodeMap, null, null);
    }

    public SearchNodeImpl(StateWrapper<TAction, TObservation, TState> wrappedState,
                          TSearchNodeMetadata searchNodeMetadata,
                          Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap,
                          SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> parent,
                          TAction appliedAction)
    {
        super(wrappedState, parent, appliedAction, searchNodeMetadata);
        if(DEBUG_ENABLED) {
            nodeId = nodeInstanceId.getAndIncrement();
        } else {
            nodeId = 0;
        }
        this.childNodeMap = childNodeMap;
    }

    @Override
    public Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> getChildNodeMap() {
        return childNodeMap;
    }

    @Override
    public String toString() {
        String metadataString = this.getSearchNodeMetadata().toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\n").append("nodeId: ");
        stringBuilder.append(nodeId);
        stringBuilder.append(metadataString);
        stringBuilder.append("\\n").append("isLeaf: ");
        stringBuilder.append(isLeaf());
        return stringBuilder.toString();
    }
}
