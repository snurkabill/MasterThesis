package vahy.impl.predictor;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class DataTableDistributionPredictorWithLr extends DataTablePredictor {

    public static final double EPSILON = Math.pow(10, -15);

    private final double learningRate;

    public DataTableDistributionPredictorWithLr(double[] defaultPrediction, double learningRate) {
        super(defaultPrediction);
        if(learningRate <= 0.0 || learningRate >= 1.0) {
            throw new IllegalArgumentException("Learning rate must be from interval (0.0, 1.0). Value: [" + learningRate + "]");
        }

        this.learningRate = learningRate;
    }

    private void trainDataSample(DoubleVector observation, double[] target) {
        double[] prediction = predictionMap.getOrDefault(observation, defaultPrediction);
        double[] newPrediction = new double[prediction.length];

        for (int i = 0; i < newPrediction.length; i++) {
//            var diffByI = - target[i] / (prediction[i] + EPSILON);
            var diffByI = - target[i] / prediction[i];
            newPrediction[i] = prediction[i] - learningRate * diffByI;
        }
        RandomDistributionUtils.applySoftmax(newPrediction);
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
