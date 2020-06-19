package vahy.api.search.nodeEvaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.search.node.nodeMetadata.BaseNodeMetadata;

import java.util.Arrays;

public abstract class AbstractNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends BaseNodeMetadata,
    TState extends State<TAction, TObservation, TState>> implements NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractNodeEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;

    protected AbstractNodeEvaluator(SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory) {
        this.searchNodeFactory = searchNodeFactory;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        if(selectedNode.getSearchNodeMetadata().isEvaluated()) {
            throw new IllegalStateException("Node is already evaluated");
        }
        var expandedNodes = 0;
        if(selectedNode.isRoot()) {
            expandedNodes += evaluateNode_inner(selectedNode);
            selectedNode.unmakeLeaf();
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        if(TRACE_ENABLED) {
            logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        }
        var childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            var stateRewardReturn = selectedNode.applyAction(nextAction);
            var childNode = searchNodeFactory.createNode(stateRewardReturn, selectedNode, nextAction);
            childNodeMap.put(nextAction, childNode);
            expandedNodes += evaluateNode_inner(childNode);
        }
        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        return expandedNodes;
    }

    protected abstract int evaluateNode_inner(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode);
}
