package vahy.paperGenerics.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
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
import java.util.Map;

public class PaperNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    implements NodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

    protected final SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    protected final Predictor<DoubleVector> trainablePredictor;
    protected final Predictor<DoubleVector> opponentPredictor;
    protected final Predictor<TState> knownModel;
    protected final TAction[] allPlayerActions;
    protected final TAction[] allOpponentActions;

    public PaperNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                              Predictor<DoubleVector> trainablePredictor,
                              Predictor<DoubleVector> opponentPredictor,
                              Predictor<TState> knownModel,
                              TAction[] allPlayerActions,
                              TAction[] allOpponentActions) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainablePredictor = trainablePredictor;
        this.opponentPredictor = opponentPredictor;
        this.allPlayerActions = allPlayerActions;
        this.allOpponentActions = allOpponentActions;
        this.knownModel = knownModel;
    }

    protected final void unmakeLeaf(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        if(!node.isFinalNode()) {
            node.unmakeLeaf();
        }
    }

    @Override
    public int evaluateNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> selectedNode) {
        var nodesExpanded = 0;
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0) {
            if(TRACE_ENABLED) {
                logger.trace("Expanding root since it is freshly created without evaluation");
            }
            nodesExpanded += innerEvaluation(selectedNode);
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        if(TRACE_ENABLED) {
            logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        }
        var childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            var nodeAndExpansions = evaluateChildNode(selectedNode, nextAction);
            childNodeMap.put(nextAction, nodeAndExpansions.getFirst());
            nodesExpanded += nodeAndExpansions.getSecond();
        }
        unmakeLeaf(selectedNode);
        return nodesExpanded;
    }

    protected void fillNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, double[] prediction, double[] opponentPrediction) {
        var searchMetadata = node.getSearchNodeMetadata();
        searchMetadata.setPredictedReward(prediction[PaperModel.Q_VALUE_INDEX]);
        searchMetadata.setExpectedReward(prediction[PaperModel.Q_VALUE_INDEX]);
        if(!node.isFinalNode()) {
            searchMetadata.setPredictedRisk(prediction[PaperModel.RISK_VALUE_INDEX]);
        }
        Map<TAction, Double> childPriorProbabilities = searchMetadata.getChildPriorProbabilities();

        if(node.getWrappedState().isPlayerTurn()) {
            evaluatePlayerNode(node, childPriorProbabilities, prediction);
        } else {
            evaluateOpponentNode(node, childPriorProbabilities, opponentPrediction);
        }
        searchMetadata.setEvaluated();
    }

    private void evaluatePlayerNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, Map<TAction, Double> childPriorProbabilities, double[] prediction) {
        TAction[] allPossibleActions = node.getAllPossibleActions();
        if(DEBUG_ENABLED) {
            for (TAction allPossibleAction : allPossibleActions) {
                if (allPossibleAction.isOpponentAction()) {
                    throw new IllegalStateException("Only player actions are available. Action set: [" + Arrays.toString(allPossibleActions) + "]");
                }
            }
        }
        if(allPlayerActions.length == allPossibleActions.length) {
            for (int i = 0; i < allPlayerActions.length; i++) {
                childPriorProbabilities.put(allPlayerActions[i], prediction[i + PaperModel.POLICY_START_INDEX]);
            }
        } else {
            double[] distribution = new double[allPlayerActions.length];
            System.arraycopy(prediction, PaperModel.POLICY_START_INDEX, distribution, 0, distribution.length);
            boolean[] mask = new boolean[allPlayerActions.length];
            for (int i = 0; i < allPossibleActions.length; i++) {
                mask[allPossibleActions[i].getActionIndexInPlayerActions()] = true;
            }
            RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);
            for (TAction key : allPossibleActions) {
                childPriorProbabilities.put(key, distribution[key.getActionIndexInPlayerActions()]);
            }
        }
    }

    protected void evaluateOpponentNode(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node, Map<TAction, Double> childPriorProbabilities, double[] probabilities) {

        //TODO: THIS METHOD IS UGLY
        TAction[] allPossibleActions = node.getAllPossibleActions();
        if(DEBUG_ENABLED) {
            for (TAction allPossibleAction : allPossibleActions) {
                if (allPossibleAction.isPlayerAction()) {
                    throw new IllegalStateException("Only opponent actions are available. Action set: [" + Arrays.toString(allPossibleActions) + "]");
                }
            }
        }

        if(knownModel == null) {
            if(allOpponentActions.length == allPossibleActions.length) {
                for (int i = 0; i < allOpponentActions.length; i++) {
                    childPriorProbabilities.put(allOpponentActions[i], probabilities[i]);
                }
            } else {
                double[] distribution = new double[allOpponentActions.length];
                System.arraycopy(probabilities, 0, distribution, 0, distribution.length);
                boolean[] mask = new boolean[allOpponentActions.length];
                for (int i = 0; i < allPossibleActions.length; i++) {
                    mask[allPossibleActions[i].getActionIndexInOpponentActions()] = true;
                }
                RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);
                for (TAction key : allPossibleActions) {
                    childPriorProbabilities.put(key, distribution[key.getActionIndexInOpponentActions()]);
                }
            }
        } else {
            if(allOpponentActions.length == allPossibleActions.length) {
                for (int i = 0; i < allOpponentActions.length; i++) {
                    childPriorProbabilities.put(allOpponentActions[i], probabilities[i]);
                }
            } else {
                double[] distribution = new double[allOpponentActions.length];
                for (int i = 0; i < allPossibleActions.length; i++) {
                    var possibleAction = allPossibleActions[i];
                    distribution[possibleAction.getActionIndexInOpponentActions()] = probabilities[i];
                }
                for (TAction key : allPossibleActions) {
                    childPriorProbabilities.put(key, distribution[key.getActionIndexInOpponentActions()]);
                }
            }
        }
    }

    protected int innerEvaluation(SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        // TODO: this is also ugly
        if(node.isPlayerTurn()) {
            fillNode(node, trainablePredictor.apply(node.getWrappedState().getPlayerObservation()), null);
        } else {
            double[] playerPrediction = trainablePredictor.apply(node.getWrappedState().getPlayerObservation());
            double[] opponentPrediction = knownModel != null ? knownModel.apply(node.getWrappedState()) : opponentPredictor.apply(node.getWrappedState().getPlayerObservation());
            fillNode(node, playerPrediction, opponentPrediction);
        }
        return 1;
    }


    private ImmutableTuple<SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState>, Integer> evaluateChildNode(
        SearchNode<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> parent,
        TAction nextAction)
    {
        var stateRewardReturn = parent.applyAction(nextAction);
        var childNode = searchNodeFactory.createNode(stateRewardReturn, parent, nextAction);
        var nodesExpanded = innerEvaluation(childNode);
        return new ImmutableTuple<>(childNode, nodesExpanded);
    }

}
