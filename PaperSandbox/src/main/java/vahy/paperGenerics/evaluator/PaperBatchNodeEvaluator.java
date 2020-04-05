package vahy.paperGenerics.evaluator;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;

public class PaperBatchNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    extends PaperNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final int maximalEvaluationDepth;

    public PaperBatchNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                   Predictor<DoubleVector> trainablePredictor,
                                   Predictor<DoubleVector> opponentApproximator,
                                   Predictor<TState> knownModel,
                                   TAction[] allPlayerActions,
                                   TAction[] allOpponentActions,
                                   int maximalEvaluationDepth) {
        super(searchNodeFactory, trainablePredictor, opponentApproximator, knownModel, allPlayerActions, allOpponentActions);
        this.maximalEvaluationDepth = maximalEvaluationDepth;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0) {
            return createSubtree(selectedNode);
        } else if(selectedNode.getChildNodeMap().isEmpty() && !selectedNode.isFinalNode()) { // TODO: .isempty should not be used. Instead alternate algo so it expands and not evaluates leaves.
            return createSubtree(selectedNode);
        }
        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        return 0;
    }

    private int createSubtree(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> rootNode) {
        var stateRewardOrder = createTreeStateSkeleton(rootNode.getWrappedState());
        var observationBatch = createObservationBatch(stateRewardOrder);
        var predictions = trainablePredictor.apply(observationBatch);
        finalizeTreeState(rootNode, stateRewardOrder, predictions);
        return stateRewardOrder.size();
    }

    private SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> createChildNode(
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction,
        StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState> stateRewardReturn,
        double[] prediction)
    {
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        fillNode(childNode, prediction);
        return childNode;
    }

    private DoubleVector[] createObservationBatch(LinkedList<StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState>> stateOrder) {
        var stateCount = stateOrder.size();
        var observationBatch = new DoubleVector[stateCount];
        var index = 0;
        for (var stateRewardEntry : stateOrder) {
            observationBatch[index] = stateRewardEntry.getState().getPlayerObservation();
            index++;
        }
        return observationBatch;
    }

    private void finalizeTreeState(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> rootNode,
                                   LinkedList<StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState>> stateOrder,
                                   double[][] predictionBatch) {
        if(predictionBatch.length != stateOrder.size()) {
            throw new IllegalStateException("Different count of predictions [" + predictionBatch.length + "] and nodes to be evaluated [" + stateOrder.size() + "]");
        }
        var queue = new LinkedList<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>>();

        fillNode(rootNode, predictionBatch[0]);
        int processedNodeCount = 1;
        stateOrder.pop(); //

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

    private LinkedList<StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState>> createTreeStateSkeleton(TState rootState) {
        var queue = new LinkedList<ImmutableTuple<TState, Integer>>();
        var nodeOrder = new LinkedList<StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState>>();

        queue.add(new ImmutableTuple<>(rootState, 0));
        nodeOrder.add(new ImmutableStateRewardReturnTuple<>(rootState, null));

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


}
