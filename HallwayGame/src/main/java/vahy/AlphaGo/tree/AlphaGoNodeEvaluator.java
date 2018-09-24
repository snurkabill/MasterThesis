package vahy.AlphaGo.tree;

import vahy.environment.ActionType;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.function.Function;

public class AlphaGoNodeEvaluator {

    private final Function<DoubleVectorialObservation, double[]> evaluationFunction;

    public AlphaGoNodeEvaluator(Function<DoubleVectorialObservation, double[]> evaluationFunction) {
        this.evaluationFunction = evaluationFunction;
    }

    public void evaluateNode(AlphaGoSearchNode node) {
        if(node.isAlreadyEvaluated()) {
            throw new IllegalStateException("Node was already evaluated");
        }
        double[] prediction = evaluationFunction.apply(node.getWrappedState().getObservation());
        node.setEstimatedReward(new DoubleScalarReward(prediction[0]));
        if(node.getWrappedState().isAgentTurn()) {
            ActionType[] playerActions = ActionType.playerActions;
            for (int i = 0; i < playerActions.length; i++) {
                node.getChildMap().get(playerActions[i]).setPriorProbability(prediction[i + 1]);
            }
        } else {
            ImmutableTuple<List<ActionType>, List<Double>> environmentActionsWithProbabilities = node.getWrappedState().environmentActionsWithProbabilities();

            for (int i = 0; i < environmentActionsWithProbabilities.getFirst().size(); i++) {
                node.getChildMap().get(environmentActionsWithProbabilities.getFirst().get(i)).setPriorProbability(environmentActionsWithProbabilities.getSecond().get(i));
            }
        }
        node.setEvaluated();
    }

}
