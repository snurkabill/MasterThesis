package vahy.tempImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.environment.MarketAction;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MarketNodeEvaluator implements TrainableNodeEvaluator<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> {

    private static final Logger logger = LoggerFactory.getLogger(MarketNodeEvaluator.class);

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    private final SearchNodeFactory<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> searchNodeFactory;
    private final TrainableApproximator<DoubleVector> trainableApproximator;

    private int nodesExpandedCount = 0;

    public MarketNodeEvaluator(SearchNodeFactory<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> searchNodeFactory,
                              TrainableApproximator<DoubleVector> trainableApproximator) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainableApproximator = trainableApproximator;
    }

    @Override
    public void evaluateNode(SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> selectedNode) {
        if(selectedNode.isRoot() && selectedNode.getSearchNodeMetadata().getVisitCounter() == 0) {
            logger.trace("Expanding root since it is freshly created without evaluation");
            innerEvaluation(selectedNode);
        }
        MarketAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<MarketAction, SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState>> childNodeMap = selectedNode.getChildNodeMap();
        for (MarketAction nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, evaluateChildNode(selectedNode, nextAction));
        }

    }

    private void innerEvaluation(SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> node) {
        nodesExpandedCount++;
        double[] prediction = trainableApproximator.apply(node.getWrappedState().getPlayerObservation());
        node.getSearchNodeMetadata().setPredictedReward(new DoubleReward(prediction[Q_VALUE_INDEX]));
        if(!node.isFinalNode()) {
            node.getSearchNodeMetadata().setPredictedRisk(prediction[RISK_VALUE_INDEX]);
        }
        Map<MarketAction, Double> childPriorProbabilities = node.getSearchNodeMetadata().getChildPriorProbabilities();
        if(node.getWrappedState().isPlayerTurn()) {
            MarketAction[] playerActions = MarketAction.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                childPriorProbabilities.put(playerActions[i], (prediction[i + POLICY_START_INDEX]));
            }
        } else {
            DoubleVector vector = node.getWrappedState().getOpponentObservation();
            MarketAction[] marketActions = MarketAction.environmentActions;
            for (int i = 0; i < marketActions.length; i++) {
                childPriorProbabilities.put(marketActions[i], vector.getObservedVector()[i]);
            }
        }
    }

    private SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> evaluateChildNode(SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> parent,
                                                                                                                                                  MarketAction nextAction) {
        StateRewardReturn<MarketAction, DoubleReward, DoubleVector, DoubleVector, MarketState> stateRewardReturn = parent.applyAction(nextAction);
        SearchNode<MarketAction, DoubleReward, DoubleVector, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> childNode = searchNodeFactory
            .createNode(stateRewardReturn, parent, nextAction);
        innerEvaluation(childNode);
        return childNode;
    }

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        trainableApproximator.train(trainData);
    }

    @Override
    public double[] evaluate(DoubleVector observation) {
        throw new UnsupportedOperationException("Not implemented now");
    }

    public int getNodesExpandedCount() {
        return nodesExpandedCount;
    }
}
