package vahy.api.predictor;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.io.Closeable;
import java.util.List;

public interface TrainablePredictor extends Predictor<DoubleVector>, Closeable {

    void train(List<ImmutableTuple<DoubleVector, double[]>> data);

    void train(ImmutableTuple<DoubleVector[], double[][]> data);

}
