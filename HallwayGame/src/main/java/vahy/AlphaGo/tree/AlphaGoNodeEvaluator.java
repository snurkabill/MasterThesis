package vahy.AlphaGo.tree;

import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.function.Function;

public class AlphaGoNodeEvaluator {

    public static final int Q_VALUE_INDEX = 0;
    public static final int RISK_VALUE_INDEX = 1;
    public static final int POLICY_START_INDEX = 2;

    private final Function<DoubleVectorialObservation, double[]> evaluationFunction;

    public AlphaGoNodeEvaluator(Function<DoubleVectorialObservation, double[]> evaluationFunction) {
        this.evaluationFunction = evaluationFunction;
    }

    public void evaluateNode(AlphaGoSearchNode node) {
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
