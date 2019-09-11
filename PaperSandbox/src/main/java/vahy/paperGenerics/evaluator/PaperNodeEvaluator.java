package vahy.paperGenerics.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.api.predictor.TrainablePredictor;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PaperNodeEvaluator<
    TAction extends Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends State<TAction, DoubleVector, TOpponentObservation, TState>>
    implements NodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);

    protected final SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    protected final TrainablePredictor<DoubleVector> trainablePredictor;
    protected final Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentpredictor;
    protected final TAction[] allPlayerActions;
    protected final TAction[] allOpponentActions;

    public PaperNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                              TrainablePredictor<DoubleVector> trainablePredictor,
                              Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentpredictor,
                              TAction[] allPlayerActions,
                              TAction[] allOpponentActions) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainablePredictor = trainablePredictor;
        this.opponentpredictor = opponentpredictor;
        this.allPlayerActions = allPlayerActions;
        this.allOpponentActions = allOpponentActions;
    }

    @Override
    public void evaluateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0) {
            logger.trace("Expanding root since it is freshly created without evaluation");
            innerEvaluation(selectedNode);
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<TAction, SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, evaluateChildNode(selectedNode, nextAction));
        }
    }

    protected void fillNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, double[] prediction) {
        var searchMetadata = node.getSearchNodeMetadata();
        searchMetadata.setPredictedReward(prediction[PaperModel.Q_VALUE_INDEX]);
        searchMetadata.setExpectedReward(prediction[PaperModel.Q_VALUE_INDEX]);
        if(!node.isFinalNode()) {
            searchMetadata.setPredictedRisk(prediction[PaperModel.RISK_VALUE_INDEX]);
        }
        Map<TAction, Double> childPriorProbabilities = searchMetadata.getChildPriorProbabilities();
        if(node.getWrappedState().isPlayerTurn()) {
            for (int i = 0; i < allPlayerActions.length; i++) {
                childPriorProbabilities.put(allPlayerActions[i], prediction[i + PaperModel.POLICY_START_INDEX]);
            }
        } else {
            evaluateOpponentNode(node, childPriorProbabilities);
        }
    }

    protected void innerEvaluation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        fillNode(node, trainablePredictor.apply(node.getWrappedState().getPlayerObservation()));
    }

    protected void evaluateOpponentNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, Map<TAction, Double> childPriorProbabilities) {
        ImmutableTuple<List<TAction>, List<Double>> probabilities = opponentpredictor.apply(node.getWrappedState().getOpponentObservation());
        for (int i = 0; i < probabilities.getFirst().size(); i++) {
            childPriorProbabilities.put(probabilities.getFirst().get(i), probabilities.getSecond().get(i));
        }
    }

    private SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> evaluateChildNode(
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction) {
        StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState> stateRewardReturn = parent.applyAction(nextAction);
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        innerEvaluation(childNode);
        return childNode;
    }

}
