package vahy.impl.predictor;

import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;

public class EmptyPredictor<TObservation extends DoubleVector> implements TrainablePredictor<TObservation> {

    private final double[] defaultPrediction;

    public EmptyPredictor(double[] defaultPrediction) {
        this.defaultPrediction = defaultPrediction;
    };

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {
        return defaultPrediction;
    }

    @Override
    public double[][] apply(TObservation[] doubleVectorialObservationArray) {
        var output = new double[doubleVectorialObservationArray.length][];
        Arrays.fill(output, defaultPrediction);
        return output;
    }
}
