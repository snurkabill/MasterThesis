package vahy.paperGenerics.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PaperNodeEvaluator<
    TAction extends Action<TAction>,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    implements NodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);

    protected final SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    protected final TrainablePredictor trainablePredictor;
    protected final Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentPredictor;
    protected final TAction[] allPlayerActions;
    protected final TAction[] allOpponentActions;

    public PaperNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                              TrainablePredictor trainablePredictor,
                              Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentPredictor,
                              TAction[] allPlayerActions,
                              TAction[] allOpponentActions) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainablePredictor = trainablePredictor;
        this.opponentPredictor = opponentPredictor;
        this.allPlayerActions = allPlayerActions;
        this.allOpponentActions = allOpponentActions;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        var nodesExpanded = 0;
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0) {
            logger.trace("Expanding root since it is freshly created without evaluation");
            nodesExpanded += innerEvaluation(selectedNode);
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<TAction, SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>> childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            var nodeAndExpansions = evaluateChildNode(selectedNode, nextAction);
            childNodeMap.put(nextAction, nodeAndExpansions.getFirst());
            nodesExpanded += nodeAndExpansions.getSecond();
        }
        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        return nodesExpanded;
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
            TAction[] allPossibleActions = node.getAllPossibleActions();
            if(allPlayerActions.length == allPossibleActions.length) {
                for (int i = 0; i < allPlayerActions.length; i++) {
                    childPriorProbabilities.put(allPlayerActions[i], prediction[i + PaperModel.POLICY_START_INDEX]);
                }
            } else {
                double[] distribution = new double[allPlayerActions.length];
                System.arraycopy(prediction, PaperModel.POLICY_START_INDEX, distribution, 0, distribution.length);
                boolean[] mask = new boolean[allPossibleActions.length];
                for (int i = 0; i < allPossibleActions.length; i++) {
                    mask[allPossibleActions[i].getActionIndexInPlayerActions()] = true;
                }
                RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);
                for (TAction key : allPossibleActions) {
                    childPriorProbabilities.put(key, distribution[key.getActionIndexInPlayerActions()]);
                }
            }
        } else {
            evaluateOpponentNode(node, childPriorProbabilities);
        }
        searchMetadata.setEvaluated();
    }

    private boolean[] createMask(TAction[] allPlayerActions, Set<TAction> keys) {
        boolean[] mask = new boolean[allPlayerActions.length];
        for (TAction key : keys) {
            mask[key.getActionIndexInPlayerActions()] = true;
        }
        return mask;
    }

    protected int innerEvaluation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        fillNode(node, trainablePredictor.apply(node.getWrappedState().getPlayerObservation()));
        return 1;
    }

    protected void evaluateOpponentNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, Map<TAction, Double> childPriorProbabilities) {
        ImmutableTuple<List<TAction>, List<Double>> probabilities = opponentPredictor.apply(node.getWrappedState().getOpponentObservation());
        for (int i = 0; i < probabilities.getFirst().size(); i++) {
            childPriorProbabilities.put(probabilities.getFirst().get(i), probabilities.getSecond().get(i));
        }
    }

    private ImmutableTuple<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>, Integer> evaluateChildNode(
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction) {
        StateRewardReturn<TAction, DoubleVector, TOpponentObservation, TState> stateRewardReturn = parent.applyAction(nextAction);
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        var nodesExpanded = innerEvaluation(childNode);
        return new ImmutableTuple<>(childNode, nodesExpanded);
    }

}
