package vahy.impl.predictor;

import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DataTablePredictor implements TrainablePredictor {

    protected final double[] defaultPrediction;
    protected HashMap<DoubleVector, double[]> predictionMap = new HashMap<>();

    public DataTablePredictor(double[] defaultPrediction) {
        this.defaultPrediction = defaultPrediction;
    }

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> data) {
        predictionMap = data
            .stream()
            .collect(Collectors
                .toMap(
                    ImmutableTuple::getFirst,
                    ImmutableTuple::getSecond,
                    (oldValue, newValue) -> oldValue,
                    HashMap::new)
            );
    }

    @Override
    public void train(ImmutableTuple<DoubleVector[], double[][]> data) {
        predictionMap = new HashMap<>();
        for (int i = 0; i < data.getFirst().length; i++) {
            var key = data.getFirst()[i];
            var value = data.getSecond()[i];
            predictionMap.put(key, value);
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
    public void close() throws IOException {

    }
}
