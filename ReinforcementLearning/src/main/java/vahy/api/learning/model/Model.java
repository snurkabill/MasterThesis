package vahy.api.learning.model;

public interface Model {

    double[] predict(double[] input);

    double[][] predict(double[][] input);

    int getInputDimension();

    int getOutputDimension();
}
