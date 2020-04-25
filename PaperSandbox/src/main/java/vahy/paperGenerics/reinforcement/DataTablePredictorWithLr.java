package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class DataTablePredictorWithLr extends DataTablePredictor {

    private final int actionCount;
    private final double learningRate;
    private final double[] killMeNow;

    public DataTablePredictorWithLr(double[] defaultPrediction, double learningRate, int actionCount) {
        super(defaultPrediction);
        this.actionCount = actionCount;
        if(learningRate <= 0.0) {
            throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
        }
        this.learningRate = learningRate;
        this.killMeNow = new double[actionCount];
    }

    private void trainDataSample(DoubleVector observation, double[] target) {
        double[] prediction = predictionMap.getOrDefault(observation, defaultPrediction);
        double[] newPrediction = new double[prediction.length];

        var rewardDiff = prediction[0] - target[0];
        newPrediction[0] = prediction[0] - learningRate * rewardDiff;

        var riskDiff = prediction[1] - target[1];
        newPrediction[1] = prediction[1] - learningRate * riskDiff;

        for (int i = 0; i < actionCount; i++) {
            var diffByI = - target[i + 2] / prediction[i + 2];
            killMeNow[i] = prediction[i + 2] - learningRate * diffByI;
        }
        RandomDistributionUtils.applySoftmax(killMeNow);
        System.arraycopy(killMeNow, 0, newPrediction, 2, actionCount);

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
