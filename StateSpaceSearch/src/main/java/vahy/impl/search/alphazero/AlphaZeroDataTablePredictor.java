package vahy.impl.search.alphazero;

import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class AlphaZeroDataTablePredictor extends DataTablePredictor {

    public static final double EPSILON = Math.pow(10, -15);

    private final double learningRate;
    private final int actionCount;
    private final int totalEntityCount;

    public AlphaZeroDataTablePredictor(double[] defaultPrediction, double learningRate, int totalEntityCount) {
        super(defaultPrediction);
        if(learningRate <= 0.0 || learningRate >= 1.0) {
            throw new IllegalArgumentException("Learning rate must be from interval (0.0, 1.0). Value: [" + learningRate + "]");
        }
        this.totalEntityCount = totalEntityCount;
        this.actionCount = defaultPrediction.length - totalEntityCount;
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

        for (int i = totalEntityCount; i < newPrediction.length; i++) {
//            var diffByI = - target[i] / (prediction[i] + EPSILON);
            var diffByI = - target[i] / prediction[i];
            probabilityDiff[i - totalEntityCount] = prediction[i] - learningRate * diffByI;
        }
        RandomDistributionUtils.applySoftmax(probabilityDiff);


//        for (int i = totalEntityCount; i < newPrediction.length; i++) {
//            probabilityDiff[i - totalEntityCount] = 1.0 / actionCount;
//        }
        System.arraycopy(probabilityDiff, 0, newPrediction, totalEntityCount, actionCount);
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
