package vahy.api.predictor;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainablePredictor<TObservation extends DoubleVector> {

    void train(List<ImmutableTuple<TObservation, double[]>> episodeData);

    double[] apply(TObservation doubleVectorialObservation);

    double[][] apply(TObservation[] doubleVectorialObservationArray);
}
