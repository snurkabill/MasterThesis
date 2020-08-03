package vahy.impl.predictor.tensorflow;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.tensorflow.TFModelImproved;

public class TensorflowTrainablePredictor implements SupervisedTrainableModel {

    private final TFModelImproved model;

    public TensorflowTrainablePredictor(TFModelImproved model) {
        this.model = model;
    }

    @Override
    public void fit(double[][] input, double[][] target) {
        model.fit(input, target);
    }

    @Override
    public double[] predict(double[] input) {
        return model.predict(input);
    }

    @Override
    public double[][] predict(double[][] input) {
        return model.predict(input);
    }

    @Override
    public int getInputDimension() {
        return model.getInputDimension();
    }

    @Override
    public int getOutputDimension() {
        return model.getOutputDimension();
    }

    @Override
    public void close() {
        model.close();
    }
}
