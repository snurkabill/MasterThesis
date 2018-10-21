package vahy.paper.tree;

import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NodeEvaluator {

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    private final Function<DoubleVectorialObservation, double[]> evaluationFunction;

    public NodeEvaluator(Function<DoubleVectorialObservation, double[]> evaluationFunction) {
        this.evaluationFunction = evaluationFunction;
    }

    public void evaluateNode(SearchNode node) {
        if(node.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be evaluated");
        }
        if(!node.isAlreadyEvaluated()) {
            innerEvaluateNode(node);
        }
        for (Map.Entry<ActionType, SearchNode> childEntry : node.getChildMap().entrySet()) {
            if(!childEntry.getValue().isFinalNode()) {
                innerEvaluateNode(childEntry.getValue());
            }
        }
    }

    public void innerEvaluateNode(SearchNode node) {
        if(node.isAlreadyEvaluated()) {
            throw new IllegalStateException("Node was already evaluated");
        }
        double[] prediction = evaluationFunction.apply(node.getWrappedState().getObservation());
        node.setEstimatedReward(new DoubleScalarReward(prediction[Q_VALUE_INDEX]));
        node.setEstimatedRisk(prediction[RISK_VALUE_INDEX]);
        if(node.getWrappedState().isAgentTurn()) {
            ActionType[] playerActions = ActionType.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                node.getEdgeMetadataMap().get(playerActions[i]).setPriorProbability(prediction[i + POLICY_START_INDEX]);
            }
        } else {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = node.getWrappedState().environmentActionsWithProbabilities();

            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                node.getEdgeMetadataMap().get(environmentActionsWithProbabilities.getFirst().get(i)).setPriorProbability(environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        node.setEvaluated();
    }

}
