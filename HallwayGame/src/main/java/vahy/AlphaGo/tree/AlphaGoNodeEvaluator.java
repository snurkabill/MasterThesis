package vahy.AlphaGo;

import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

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
        double[] nodePriorProbabilities = node.getPriorProbabilities();
        System.arraycopy(prediction, 1, nodePriorProbabilities, 0, nodePriorProbabilities.length);
        node.setEvaluated();
    }

}
