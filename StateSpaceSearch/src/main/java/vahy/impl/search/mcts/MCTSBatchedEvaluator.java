package vahy.impl.search.mcts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;

public class MCTSBatchedEvaluator<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, DoubleVector, TState>>
    implements NodeEvaluator<TAction, DoubleVector, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(MCTSBatchedEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory;
    private final TrainablePredictor predictor;
    private final int maximalEvaluationDepth;


    public MCTSBatchedEvaluator(SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory, TrainablePredictor predictor, int maximalEvaluationDepth) {
        this.predictor = predictor;
        this.searchNodeFactory = searchNodeFactory;
        this.maximalEvaluationDepth = maximalEvaluationDepth;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> selectedNode) {
        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0 || selectedNode.getChildNodeMap().isEmpty()) {

            var expandedNodeCount = createSubtree(selectedNode);
//            logger.info("ExpandedNodeCount: [{}]", expandedNodeCount);
            return expandedNodeCount;
        }
        return 0;
    }

    private int createSubtree(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode) {
        var stateRewardOrder = createTreeStateSkeleton(rootNode.getStateWrapper());
        int nodeCount = stateRewardOrder.size();

        var observationBatch = createObservationBatch(stateRewardOrder);
        var predictions = predictor.apply(observationBatch);
        finalizeTreeState(rootNode, stateRewardOrder, predictions);
        return nodeCount;
    }

    private SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> createChildNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> parent,
                                                                                           TAction nextAction,
                                                                                           StateWrapperRewardReturn<TAction, DoubleVector, TState> stateRewardReturn,
                                                                                           double[] valuePrediction)
    {
        SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        fillNode(childNode, valuePrediction);
        return childNode;
    }

    private DoubleVector[] createObservationBatch(LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> stateOrder) {
        var stateCount = stateOrder.size();
        var observationBatch = new DoubleVector[stateCount];
        var index = 0;
        for (var stateRewardEntry : stateOrder) {
            observationBatch[index] = stateRewardEntry.getState().getObservation();
            index++;
        }
        return observationBatch;
    }

    private void finalizeTreeState(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode,
                                   LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> stateOrder,
                                   double[][] predictionBatch) {
        if(predictionBatch.length != stateOrder.size()) {
            throw new IllegalStateException("Different count of predictions [" + predictionBatch.length + "] and nodes to be evaluated [" + stateOrder.size() + "]");
        }
        var queue = new LinkedList<SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState>>();

        fillNode(rootNode, predictionBatch[0]);
        int processedNodeCount = 1;
        stateOrder.pop();

        queue.add(rootNode);
        while(processedNodeCount < predictionBatch.length) {
            var node = queue.pop();
            node.getSearchNodeMetadata().setEvaluated();
            if(!node.isFinalNode()) {
                TAction[] allPossibleActions = node.getAllPossibleActions();
                var childNodeMap = node.getChildNodeMap();
                for (TAction nextAction : allPossibleActions) {
                    if(processedNodeCount >= predictionBatch.length) {
                        throw new IllegalStateException("There are still children to fill, but not enough predictions");
                    }
                    var stateRewardReturn = stateOrder.pop();

                    var childNode = createChildNode(node, nextAction, stateRewardReturn, predictionBatch[processedNodeCount]);
                    childNodeMap.put(nextAction, childNode);
                    queue.add(childNode);
                    processedNodeCount++;
                }
            }

        }
    }

    private LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> createTreeStateSkeleton(StateWrapper<TAction, DoubleVector, TState> rootState) {
        var queue = new LinkedList<ImmutableTuple<StateWrapper<TAction, DoubleVector, TState>, Integer>>();
        var nodeOrder = new LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>>();

        queue.add(new ImmutableTuple<>(rootState, 0));
        nodeOrder.add(new ImmutableStateWrapperRewardReturn<TAction, DoubleVector, TState>(rootState, 0.0, null));

        while(!queue.isEmpty()) {
            var stateTuple = queue.pop();
            var depth = stateTuple.getSecond();
            var state = stateTuple.getFirst();


            if(!state.isFinalState()) {
                TAction[] allPossibleActions = state.getAllPossibleActions();
                for (TAction nextAction : allPossibleActions) {
                    var childStateReward = state.applyAction(nextAction);
                    nodeOrder.add(childStateReward);
                    if(depth + 1 <= maximalEvaluationDepth) {
                        queue.add(new ImmutableTuple<>(childStateReward.getState(), depth + 1));
                    }
                }
            }
        }
        return nodeOrder;
    }

    protected void fillNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> node, double[] valuePrediction) {
        var searchMetadata = node.getSearchNodeMetadata();
        var expectedReward = searchMetadata.getExpectedReward();
        System.arraycopy(valuePrediction, 0, expectedReward, 0, valuePrediction.length);
        searchMetadata.setEvaluated();
    }

}
