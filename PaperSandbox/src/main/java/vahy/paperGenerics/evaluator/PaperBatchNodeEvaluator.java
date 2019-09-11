package vahy.paperGenerics.evaluator;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.metadata.PaperBatchedMetadata;
import vahy.api.predictor.TrainablePredictor;
import vahy.utils.ImmutableTuple;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class PaperBatchNodeEvaluator<
    TAction extends Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperBatchedMetadata<TAction>,
    TState extends State<TAction, DoubleVector, TOpponentObservation, TState>>
    extends PaperNodeEvaluator<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final int maximalEvaluationDepth;

    public PaperBatchNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                   TrainablePredictor<DoubleVector> trainablePredictor,
                                   Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentApproximator,
                                   TAction[] allPlayerActions,
                                   TAction[] allOpponentActions,
                                   int maximalEvaluationDepth) {
        super(searchNodeFactory, trainablePredictor, opponentApproximator, allPlayerActions, allOpponentActions);
        this.maximalEvaluationDepth = maximalEvaluationDepth;
    }

    @Override
    public void evaluateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(!selectedNode.getSearchNodeMetadata().isVisible()) {
            createSubtree(selectedNode);
        }
    }

    private void createSubtree(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> rootNode) {
        var stateRewardOrder = createTreeStateSkeleton(rootNode.getWrappedState());
        var observationBatch = createObservationBatch(stateRewardOrder);
        var predictions = trainablePredictor.apply(observationBatch);
        finalizeTreeState(rootNode, stateRewardOrder, predictions);
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
        var queue = new LinkedList<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>>();

        queue.add(rootNode);
        int processedNodeCount = 0;

        while(processedNodeCount < predictionBatch.length) {
            var node = queue.pop();
            node.getSearchNodeMetadata().setVisible();
            TAction[] allPossibleActions = node.getAllPossibleActions();
            var childNodeMap = node.getChildNodeMap();
            for (TAction nextAction : allPossibleActions) {
                var stateRewardReturn = stateOrder.pop();
                var childNode = createChildNode(node, nextAction, stateRewardReturn, predictionBatch[processedNodeCount]);
                childNodeMap.put(nextAction, childNode);
                queue.add(childNode);
                processedNodeCount++;
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
            TAction[] allPossibleActions = state.getAllPossibleActions();
            for (TAction nextAction : allPossibleActions) {
                var childStateReward = state.applyAction(nextAction);
                nodeOrder.add(childStateReward);
                if(depth + 1 <= maximalEvaluationDepth) {
                    queue.add(new ImmutableTuple<>(childStateReward.getState(), depth + 1));
                }
            }
        }
        return nodeOrder;
    }


}
