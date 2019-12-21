package vahy.api.predictor;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.io.Closeable;
import java.util.List;

public interface TrainablePredictor extends Closeable {

    void train(List<ImmutableTuple<DoubleVector, double[]>> data);

    void train(ImmutableTuple<DoubleVector[], double[][]> data);

    double[] apply(DoubleVector observation);

    double[][] apply(DoubleVector[] observationArray);
}
