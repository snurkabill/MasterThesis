package vahy.paperGenerics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaperNodeEvaluator implements NodeEvaluator<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    private final SearchNodeFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> searchNodeFactory;
    private final TrainableApproximator<DoubleVectorialObservation> trainableApproximator;

    public PaperNodeEvaluator(SearchNodeFactory<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> searchNodeFactory,
                              TrainableApproximator<DoubleVectorialObservation> trainableApproximator) {
        this.searchNodeFactory = searchNodeFactory;
        this.trainableApproximator = trainableApproximator;
    }

    @Override
    public void evaluateNode(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> selectedNode) {
        ActionType[] allPossibleActions = selectedNode.getAllPossibleActions();
        logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        Map<ActionType, SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl>> childNodeMap = selectedNode.getChildNodeMap();
        for (ActionType nextAction : allPossibleActions) {
            childNodeMap.put(nextAction, evaluateChildNode(selectedNode, nextAction));
        }
    }

    private SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> evaluateChildNode(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> parent,
                                                                                                                                                                        ActionType nextAction) {
        StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, ImmutableStateImpl> stateRewardReturn = parent.applyAction(nextAction);
        SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> childNode = searchNodeFactory
            .createNode(stateRewardReturn, parent, nextAction);
        double[] prediction = trainableApproximator.apply(stateRewardReturn.getState().getObservation());
        childNode.getSearchNodeMetadata().setPredictedReward(new DoubleScalarReward(prediction[Q_VALUE_INDEX]));
        childNode.getSearchNodeMetadata().setPredictedRisk(prediction[RISK_VALUE_INDEX]);
        Map<ActionType, Double> childPriorProbabilities = childNode.getSearchNodeMetadata().getChildPriorProbabilities();
        if(childNode.getWrappedState().isPlayerTurn()) {
            ActionType[] playerActions = ActionType.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                childPriorProbabilities.put(playerActions[i], (prediction[i + POLICY_START_INDEX]));
            }
        } else {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = childNode.getWrappedState().environmentActionsWithProbabilities();
            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                childPriorProbabilities.put(environmentActionsWithProbabilities.getFirst().get(i), environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        return childNode;
    }
}
