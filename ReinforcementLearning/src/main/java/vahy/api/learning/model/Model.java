package vahy.api.learning.model;

import java.io.Closeable;

public interface Model extends Closeable {

    double[] predict(double[] input);

    double[][] predict(double[][] input);

    int getInputDimension();

    int getOutputDimension();
}
