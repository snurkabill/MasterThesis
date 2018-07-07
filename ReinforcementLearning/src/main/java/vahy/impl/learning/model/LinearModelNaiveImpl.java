package vahy.impl.learning.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.learning.model.SupervisedTrainableModel;

public class LinearModelNaiveImpl implements SupervisedTrainableModel {

    private static final Logger logger = LoggerFactory.getLogger(LinearModelNaiveImpl.class);

    private final int inputDimension;
    private final int outputDimension;
    private final double weightMatrix[][];
    private final double biasArray[];
    private final double learningRate;

    public LinearModelNaiveImpl(int inputDimension,
                                int outputDimension,
                                double learningRate) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
        this.learningRate = learningRate;
        this.weightMatrix = new double[outputDimension][];
        for (int i = 0; i < weightMatrix.length; i++) {
            this.weightMatrix[i] = new double[inputDimension];
        }
        this.biasArray = new double[outputDimension];
        logger.debug("Created [{}] with input size [{}] and outputs size [{}]", LinearModelNaiveImpl.class.getName(), inputDimension, outputDimension);
    }

    @Override
    public int getInputDimension() {
        return this.inputDimension;
    }

    @Override
    public int getOutputDimension() {
        return this.outputDimension;
    }

    @Override
    public double[] predict(double[] input) {
        logger.trace("Predicting input in naive linear model");
        if(input.length != this.inputDimension) {
            throw new IllegalArgumentException("Expected input length: [" + inputDimension + "]. Actual length: [" + input.length + "]");
        }
        double[] output = new double[outputDimension];
        for (int i = 0; i < biasArray.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < weightMatrix[0].length; j++) {
                sum += input[j] * weightMatrix[i][j];
            }
            sum += biasArray[i];
            output[i] = sum;
        }
        return output;
    }

    @Override
    public double[][] predict(double[][] input) {
        double[][] outputMatrix = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            outputMatrix[i] = predict(input[i]);
        }
        return outputMatrix;
    }

    @Override
    public void fit(double[][] input, double[][] target) {
        logger.debug("Fitting model [{}] with data [{}] data samples", this.toString(), input.length);
        for (int i = 0; i < input.length; i++) {
            double[] predicted = predict(input[i]);
            double[] expected = target[i];
            if(input[0].length != weightMatrix[0].length) {
                throw new IllegalStateException("SafetyCheck - input size is different than weightInputSize");
            }
            for (int j = 0; j < predicted.length; j++) {
                double diff = predicted[j] - expected[j];
                for (int k = 0; k < weightMatrix[0].length; k++) {
                    weightMatrix[j][k] = weightMatrix[j][k] - learningRate * diff * input[i][k];
                }
                biasArray[j] = biasArray[j] - learningRate * diff;
            }
        }
    }
}
