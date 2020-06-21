package vahy.impl.predictor;

import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataTablePredictorWithLr implements TrainablePredictor {

    private final double learningRate;
    private final double[] defaultPrediction;
    private final HashMap<DoubleVector, double[]> predictionMap = new HashMap<>();

    public DataTablePredictorWithLr(double[] defaultPrediction, double learningRate) {
        this.learningRate = learningRate;
        this.defaultPrediction = defaultPrediction;
    }

    private void trainDataSample(DoubleVector observation, double[] target) {
        double[] prediction = predictionMap.getOrDefault(observation, defaultPrediction);
        double[] newPrediction = new double[prediction.length];

        for (int i = 0; i < prediction.length; i++) {
            var rewardDiff = prediction[i] - target[i];
            newPrediction[i] = prediction[i] - learningRate * rewardDiff;
        }
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

    @Override
    public double[] apply(DoubleVector doubleObservation) {
        return predictionMap.getOrDefault(doubleObservation, defaultPrediction);
    }

    @Override
    public double[][] apply(DoubleVector[] doubleObservationArray) {
        double[][] input = new double[doubleObservationArray.length][];
        for (int i = 0; i < doubleObservationArray.length; i++) {
            input[i] = predictionMap.getOrDefault(doubleObservationArray[i], defaultPrediction);
        }
        return input;
    }

    @Override
    public List<double[]> apply(List<DoubleVector> doubleVectors) {
        var output = new ArrayList<double[]>(doubleVectors.size());
        for (int i = 0; i < doubleVectors.size(); i++) {
            output.add(predictionMap.getOrDefault(doubleVectors.get(i), defaultPrediction));
        }
        return output;
    }

    @Override
    public void close() {

    }
}
