package vahy.impl.predictor;

import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DataTablePredictor<TObservation extends DoubleVector> implements TrainablePredictor<TObservation> {

    protected final double[] defaultPrediction;
    protected HashMap<TObservation, double[]> predictionMap = new HashMap<>();

    public DataTablePredictor(double[] defaultPrediction) {
        this.defaultPrediction = defaultPrediction;
    }

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
        predictionMap = episodeData
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
    public double[] apply(TObservation doubleVectorialObservation) {
        return predictionMap.getOrDefault(doubleVectorialObservation, defaultPrediction);
    }

    @Override
    public double[][] apply(TObservation[] doubleVectorialObservationArray) {
        double[][] input = new double[doubleVectorialObservationArray.length][];
        for (int i = 0; i < doubleVectorialObservationArray.length; i++) {
            input[i] = predictionMap.getOrDefault(doubleVectorialObservationArray[i], defaultPrediction);
        }
        return input;
    }
}
