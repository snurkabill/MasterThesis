package vahy.ralph.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class RalphDataTablePredictorWithLr extends DataTablePredictor {

    private final int actionCount;
    private final double learningRate;
    private final int totalEntityCount;

    public RalphDataTablePredictorWithLr(double[] defaultPrediction, double learningRate, int actionCount, int totalEntityCount) {
        super(defaultPrediction);
        this.actionCount = actionCount;
        this.totalEntityCount = totalEntityCount;
        if(learningRate <= 0.0) {
            throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
        }
        this.learningRate = learningRate;
    }

    private void trainDataSample(DoubleVector observation, double[] target) {
        double[] prediction = predictionMap.getOrDefault(observation, defaultPrediction);
        double[] newPrediction = new double[prediction.length];
        double[] probabilityDiff = new double[actionCount];

        for (int i = 0; i < totalEntityCount; i++) {
            var rewardDiff = prediction[i] - target[i];
            newPrediction[i] = prediction[i] - learningRate * rewardDiff;
        }

        for (int i = totalEntityCount; i < totalEntityCount * 2; i++) {
            var riskDiff = prediction[i] - target[i];
            newPrediction[i] = prediction[i] - learningRate * riskDiff;
        }

        for (int i = totalEntityCount * 2; i < newPrediction.length; i++) {
//            var diffByI = - target[i] / (prediction[i] + EPSILON);
            var diffByI = - target[i] / prediction[i];
            probabilityDiff[i - totalEntityCount * 2] = prediction[i] - learningRate * diffByI;
        }
        RandomDistributionUtils.applySoftmax(probabilityDiff);
        System.arraycopy(probabilityDiff, 0, newPrediction, totalEntityCount * 2, actionCount);

        predictionMap.put(observation, newPrediction);
    }

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> data) {
        for (ImmutableTuple<DoubleVector, double[]> entry : data) {
            trainDataSample(entry.getFirst(), entry.getSecond());
        }
    }

    @Override
    public void train(ImmutableTuple<DoubleVector[], double[][]> data) {
        var observations = data.getFirst();
        var targets = data.getSecond();
        for (int i = 0; i < observations.length; i++) {
            trainDataSample(observations[i], targets[i]);
        }
    }
}
