package vahy.api.search.nodeEvaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.predictor.Predictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.search.node.nodeMetadata.BaseNodeMetadata;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;

public abstract class AbstractBatchedNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends BaseNodeMetadata,
    TState extends State<TAction, DoubleVector, TState>> implements NodeEvaluator<TAction, DoubleVector, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBatchedNodeEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory;
    private final Predictor<DoubleVector> predictor;
    private final int maximalEvaluationDepth;

    protected AbstractBatchedNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory, Predictor<DoubleVector> predictor, int maximalEvaluationDepth) {
        this.searchNodeFactory = searchNodeFactory;
        this.predictor = predictor;
        this.maximalEvaluationDepth = maximalEvaluationDepth;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        selectedNode.unmakeLeaf();

        if(selectedNode.isRoot() || selectedNode.getChildNodeMap().isEmpty()) {
            return createSubtree(selectedNode);
        }
        return 0;
    }

//    protected abstract int evaluateNode_inner(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> selectedNode);

    private int createSubtree(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode) {
        var stateRewardOrder = createTreeStateSkeleton(rootNode.getStateWrapper());
        int nodeCount = stateRewardOrder.size();

        var observationBatch = createObservationBatch(stateRewardOrder);
        var predictions = predictor.apply(observationBatch);
        finalizeTreeState(rootNode, stateRewardOrder, predictions);
        return nodeCount;
    }

    private LinkedList<ImmutableTuple<Integer, StateWrapperRewardReturn<TAction, DoubleVector, TState>>> createTreeStateSkeleton(StateWrapper<TAction, DoubleVector, TState> rootState) {
        var queue = new LinkedList<ImmutableTuple<StateWrapper<TAction, DoubleVector, TState>, Integer>>();
        var nodeOrder = new LinkedList<ImmutableTuple<Integer, StateWrapperRewardReturn<TAction, DoubleVector, TState>>>();

        int check = 0;
        queue.add(new ImmutableTuple<>(rootState, 0));
        nodeOrder.add(new ImmutableTuple<>(check, new ImmutableStateWrapperRewardReturn<TAction, DoubleVector, TState>(rootState, 0.0, null)));
        check++;

        while(!queue.isEmpty()) {
            var stateTuple = queue.pop();
            var depth = stateTuple.getSecond();
            var state = stateTuple.getFirst();

            if(!state.isFinalState()) {
                TAction[] allPossibleActions = state.getAllPossibleActions();
                for (TAction nextAction : allPossibleActions) {
                    var childStateReward = state.applyAction(nextAction);
                    nodeOrder.add(new ImmutableTuple<>(check, childStateReward));
                    check++;
                    if(depth + 1 <= maximalEvaluationDepth) {
                        queue.add(new ImmutableTuple<>(childStateReward.getState(), depth + 1));
                    }
                }
            }
        }
        return nodeOrder;
    }

    private void finalizeTreeState(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode,
                                   LinkedList<ImmutableTuple<Integer, StateWrapperRewardReturn<TAction, DoubleVector, TState>>> stateOrder,
                                   double[][] predictionBatch) {
        if(predictionBatch.length != stateOrder.size()) {
            throw new IllegalStateException("Different count of predictions [" + predictionBatch.length + "] and nodes to be evaluated [" + stateOrder.size() + "]");
        }
        var queue = new LinkedList<SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState>>();

        int processedNodeCount = 0;
        fillNode(rootNode, predictionBatch[processedNodeCount]);
        processedNodeCount++;
        stateOrder.pop();

        queue.add(rootNode);
        while(processedNodeCount < predictionBatch.length) {
            var node = queue.pop();
            if(!node.isFinalNode()) {
                TAction[] allPossibleActions = node.getAllPossibleActions();
                var childNodeMap = node.getChildNodeMap();
                for (TAction nextAction : allPossibleActions) {
                    if(processedNodeCount >= predictionBatch.length) {
                        throw new IllegalStateException("There are still children to fill, but not enough predictions");
                    }
                    var stateRewardReturn = stateOrder.pop();
                    if(processedNodeCount != stateRewardReturn.getFirst()) {
                        throw new IllegalStateException("Inconsistency");
                    }

                    var childNode = createChildNode(node, nextAction, stateRewardReturn.getSecond(), predictionBatch[processedNodeCount]);
                    childNodeMap.put(nextAction, childNode);
                    queue.add(childNode);
                    processedNodeCount++;
                }
            }

        }
    }

    private SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> createChildNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> parent,
                                                                                          TAction nextAction,
                                                                                          StateWrapperRewardReturn<TAction, DoubleVector, TState> stateRewardReturn,
                                                                                          double[] prediction)
    {
        SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        fillNode(childNode, prediction);
        return childNode;
    }

    protected abstract void fillNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> node, double[] prediction);

    protected DoubleVector[] createObservationBatch(LinkedList<ImmutableTuple<Integer, StateWrapperRewardReturn<TAction, DoubleVector, TState>>> stateOrder)
    {
        var stateCount = stateOrder.size();
        var observationBatch = new DoubleVector[stateCount];
        var index = 0;
        for (var stateRewardEntry : stateOrder) {
            observationBatch[index] = stateRewardEntry.getSecond().getState().getObservation();
            if(index != stateRewardEntry.getFirst()) {
                throw new IllegalStateException("Inconsistency");
            }
            index++;
        }
        return observationBatch;
    }


}