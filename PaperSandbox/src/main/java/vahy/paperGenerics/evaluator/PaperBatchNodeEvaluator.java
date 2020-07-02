//package vahy.paperGenerics.evaluator;
//
//import vahy.api.model.Action;
//import vahy.api.model.StateWrapper;
//import vahy.api.model.StateWrapperRewardReturn;
//import vahy.api.predictor.Predictor;
//import vahy.api.search.node.SearchNode;
//import vahy.api.search.node.factory.SearchNodeFactory;
//import vahy.impl.model.ImmutableStateWrapperRewardReturn;
//import vahy.impl.model.observation.DoubleVector;
//import vahy.paperGenerics.PaperState;
//import vahy.paperGenerics.metadata.PaperMetadata;
//import vahy.utils.ImmutableTuple;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//
//public class PaperBatchNodeEvaluator<
//    TAction extends Enum<TAction> & Action,
//    TSearchNodeMetadata extends PaperMetadata<TAction>,
//    TState extends PaperState<TAction, DoubleVector, TState>>
//    extends PaperNodeEvaluator<TAction, TSearchNodeMetadata, TState> {
//
//    // TODO: this file is also quite ugly.
//
//    private final int maximalEvaluationDepth;
//    private final TState[] stateArray;
//
//    public PaperBatchNodeEvaluator(
//                                   SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory,
//                                   Predictor<DoubleVector> trainablePredictor,
//                                   Predictor<DoubleVector> opponentApproximator,
//                                   Predictor<TState> knownModel,
//                                   TAction[] allPlayerActions,
//                                   TAction[] allOpponentActions,
//                                   int maximalEvaluationDepth,
//                                   TState[] stateArray) {
//        super(searchNodeFactory, trainablePredictor, opponentApproximator, knownModel, allPlayerActions, allOpponentActions);
//        this.maximalEvaluationDepth = maximalEvaluationDepth;
//        this.stateArray = stateArray;
//    }
//
//    @Override
//    public int evaluateNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> selectedNode) {
//        unmakeLeaf(selectedNode);
//        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0 || selectedNode.getChildNodeMap().isEmpty()) {
//            return createSubtree(selectedNode);
//        }
//        return 0;
//    }
//
//    private int createSubtree(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode) {
//        var stateRewardOrder = createTreeStateSkeleton(rootNode.getStateWrapper());
//        int nodeCount = stateRewardOrder.size();
//        if(knownModel != null) {
//            var observationBatch = createObservationBatchForKnownModel(stateRewardOrder);
//            var predictions = trainablePredictor.apply(observationBatch.getFirst());
//            var opponentPredictions = knownModel.apply(observationBatch.getSecond());
//            finalizeTreeState(rootNode, stateRewardOrder, predictions, opponentPredictions);
//        } else {
//            var observationBatch = createObservationBatch(stateRewardOrder);
//            var predictions = trainablePredictor.apply(observationBatch.getFirst());
//            var opponentPredictions = opponentPredictor.apply(observationBatch.getSecond());
//            finalizeTreeState(rootNode, stateRewardOrder, predictions, opponentPredictions);
//        }
//        return nodeCount;
//    }
//
//    private SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> createChildNode(
//        SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> parent,
//        TAction nextAction,
//        StateWrapperRewardReturn<TAction, DoubleVector, TState> stateRewardReturn,
//        double[] prediction,
//        double[] opponentPrediction)
//    {
//        SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
//        fillNode(childNode, prediction, opponentPrediction);
//        return childNode;
//    }
//
//    private ImmutableTuple<DoubleVector[], TState[]> createObservationBatchForKnownModel(LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> stateOrder) {
//        var stateCount = stateOrder.size();
//        var observationBatch = new DoubleVector[stateCount];
//        var opponentObservationBatch = new ArrayList<StateWrapper<TAction, DoubleVector, TState>>();
//        var index = 0;
//        for (var stateRewardEntry : stateOrder) {
//            observationBatch[index] = stateRewardEntry.getState().getObservation();
//            if(!stateRewardEntry.getState().isPlayerTurn()) {
//                opponentObservationBatch.add(stateRewardEntry.getState());
//            }
//            index++;
//        }
//        return new ImmutableTuple<>(observationBatch, opponentObservationBatch.toArray(stateArray));
//    }
//
//    private ImmutableTuple<DoubleVector[], DoubleVector[]> createObservationBatch(LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> stateOrder) {
//        var stateCount = stateOrder.size();
//        var observationBatch = new DoubleVector[stateCount];
//        var opponentObservationBatch = new ArrayList<DoubleVector>();
//        var index = 0;
//        for (var stateRewardEntry : stateOrder) {
//            observationBatch[index] = stateRewardEntry.getState().getObservation();
//            if(!stateRewardEntry.getState().isPlayerTurn()) {
//                opponentObservationBatch.add(stateRewardEntry.getState().getCommonObservation());
//            }
//            index++;
//        }
//        return new ImmutableTuple<>(observationBatch, opponentObservationBatch.toArray(DoubleVector[]::new));
//    }
//
//    private void finalizeTreeState(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> rootNode,
//                                   LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> stateOrder,
//                                   double[][] predictionBatch,
//                                   double[][] opponentPredictionBatch) {
//        if(predictionBatch.length != stateOrder.size()) {
//            throw new IllegalStateException("Different count of predictions [" + predictionBatch.length + "] and nodes to be evaluated [" + stateOrder.size() + "]");
//        }
//        var queue = new LinkedList<SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState>>();
//
//        fillNode(rootNode, predictionBatch[0], rootNode.isOpponentTurn() ? opponentPredictionBatch[0] : null);
//        int processedNodeCount = 1;
//        int processedOpponentBatchCount = rootNode.isOpponentTurn() ? 1 : 0;
//        stateOrder.pop(); //
//
//        queue.add(rootNode);
//        while(processedNodeCount < predictionBatch.length) {
//            var node = queue.pop();
//            node.getSearchNodeMetadata().setEvaluated();
//            if(!node.isFinalNode()) {
//                TAction[] allPossibleActions = node.getAllPossibleActions();
//                var childNodeMap = node.getChildNodeMap();
//                for (TAction nextAction : allPossibleActions) {
//                    if(processedNodeCount >= predictionBatch.length) {
//                        throw new IllegalStateException("There are still children to fill, but not enough predictions");
//                    }
//                    var stateRewardReturn = stateOrder.pop();
//                    if(stateRewardReturn.getState().isPlayerTurn()) {
//                        var childNode = createChildNode(node, nextAction, stateRewardReturn, predictionBatch[processedNodeCount], null);
//                        childNodeMap.put(nextAction, childNode);
//                        queue.add(childNode);
//                        processedNodeCount++;
//                    } else {
//                        var childNode = createChildNode(node, nextAction, stateRewardReturn, predictionBatch[processedNodeCount], opponentPredictionBatch[processedOpponentBatchCount]);
//                        childNodeMap.put(nextAction, childNode);
//                        queue.add(childNode);
//                        processedNodeCount++;
//                        processedOpponentBatchCount++;
//                    }
//
//                }
//            }
//
//        }
//    }
//
//    private LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>> createTreeStateSkeleton(StateWrapper<TAction, DoubleVector, TState> rootState) {
//        var queue = new LinkedList<ImmutableTuple<StateWrapper<TAction, DoubleVector, TState>, Integer>>();
//        var nodeOrder = new LinkedList<StateWrapperRewardReturn<TAction, DoubleVector, TState>>();
//
//        queue.add(new ImmutableTuple<>(rootState, 0));
//        nodeOrder.add(new ImmutableStateWrapperRewardReturn<>(rootState, null));
//
//        while(!queue.isEmpty()) {
//            var stateTuple = queue.pop();
//            var depth = stateTuple.getSecond();
//            var state = stateTuple.getFirst();
//
//            if(!state.isFinalState()) {
//                TAction[] allPossibleActions = state.getAllPossibleActions();
//                for (TAction nextAction : allPossibleActions) {
//                    var childStateReward = state.applyAction(nextAction);
//                    nodeOrder.add(childStateReward);
//                    if(depth + 1 <= maximalEvaluationDepth) {
//                        queue.add(new ImmutableTuple<>(childStateReward.getState(), depth + 1));
//                    }
//                }
//            }
//        }
//        return nodeOrder;
//    }
//
//
//}
