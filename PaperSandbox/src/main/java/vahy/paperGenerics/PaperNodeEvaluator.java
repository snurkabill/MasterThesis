package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaperNodeEvaluator implements NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    private final SearchNodeFactory<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> searchNodeFactory;
    private final TrainableApproximator<DoubleVector> trainableApproximator;

    public PaperNodeEvaluator(SearchNodeFactory<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> searchNodeFactory,
                              TrainableApproximator<DoubleVector> trainableApproximator) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainableApproximator = trainableApproximator;
    }

    @Override
    public void evaluateNode(SearchNode<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> selectedNode) {
        HallwayAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<HallwayAction, SearchNode<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>> childNodeMap = selectedNode.getChildNodeMap();
        for (HallwayAction nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, evaluateChildNode(selectedNode, nextAction));
        }
    }

    private SearchNode<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> evaluateChildNode(SearchNode<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> parent,
                                                                                                                                                  HallwayAction nextAction) {
        StateRewardReturn<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> stateRewardReturn = parent.applyAction(nextAction);
        SearchNode<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> childNode = searchNodeFactory
            .createNode(stateRewardReturn, parent, nextAction);
        double[] prediction = trainableApproximator.apply(stateRewardReturn.getState().getObservation());
        childNode.getSearchNodeMetadata().setPredictedReward(new DoubleReward(prediction[Q_VALUE_INDEX]));
        childNode.getSearchNodeMetadata().setPredictedRisk(prediction[RISK_VALUE_INDEX]);
        Map<HallwayAction, Double> childPriorProbabilities = childNode.getSearchNodeMetadata().getChildPriorProbabilities();
        if(childNode.getWrappedState().isPlayerTurn()) {
            HallwayAction[] playerActions = HallwayAction.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                childPriorProbabilities.put(playerActions[i], (prediction[i + POLICY_START_INDEX]));
            }
        } else {
            ImmutableTuple<List<HallwayAction>, List<Double>> environmentActionsWithProbabilities = childNode.getWrappedState().environmentActionsWithProbabilities();
            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                childPriorProbabilities.put(environmentActionsWithProbabilities.getFirst().get(i), environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        return childNode;
    }
}
