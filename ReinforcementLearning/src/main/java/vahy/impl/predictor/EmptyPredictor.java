package vahy.impl.predictor;

import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmptyPredictor implements TrainablePredictor {

    private final double[] defaultPrediction;

    public EmptyPredictor(double[] defaultPrediction) {
        this.defaultPrediction = defaultPrediction;
    };

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> data) {
    }

    @Override
    public void train(ImmutableTuple<DoubleVector[], double[][]> data) {
    }

    @Override
    public double[] apply(DoubleVector doubleObservation) {
        return defaultPrediction;
    }

    @Override
    public double[][] apply(DoubleVector[] doubleObservationArray) {
        var output = new double[doubleObservationArray.length][];
        Arrays.fill(output, defaultPrediction);
        return output;
    }

    @Override
    public List<double[]> apply(List<DoubleVector> doubleVectors) {
        var output = new ArrayList<double[]>(doubleVectors.size());
        for (int i = 0; i < doubleVectors.size(); i++) {
            output.add(defaultPrediction);
        }
        return output;
    }

    @Override
    public void close() {

    }
}
