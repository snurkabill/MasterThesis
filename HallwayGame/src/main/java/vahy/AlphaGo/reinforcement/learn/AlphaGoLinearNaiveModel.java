package vahy.AlphaGo.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.impl.learning.model.LinearModelNaiveImpl;

public class AlphaGoLinearNaiveModel extends LinearModelNaiveImpl {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoLinearNaiveModel.class);

    public AlphaGoLinearNaiveModel(int inputDimension, int outputDimension, double learningRate) {
        super(inputDimension, outputDimension, learningRate);
    }

    @Override
    public double[] predict(double[] input) {
        logger.trace("Predicting input in naive linear model");
        if(input.length != this.getInputDimension()) {
            throw new IllegalArgumentException("Expected input length: [" + this.getInputDimension() + "]. Actual length: [" + input.length + "]");
        }
        double[] output = new double[getOutputDimension()];
        calculateOutputPotentials(input, output);
        normalizeWithSoftmaxLeavingFirstElementUntouched(output);
        return output;
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
                double diff =  // predicted[j] - expected[j];
                    j == 0 ?  // first element is
                        predicted[j] - expected[j] :
                        expected[j] / (predicted[j] + Double.MIN_VALUE); //  + (1 - expected[j]) / ((1 - predicted[j]) + Double.MIN_VALUE) ;
                for (int k = 0; k < weightMatrix[0].length; k++) {
                    weightMatrix[j][k] = weightMatrix[j][k] - learningRate * diff * input[i][k];
                }
                biasArray[j] = biasArray[j] - learningRate * diff;
            }
        }

    }

    private void normalizeWithSoftmaxLeavingFirstElementUntouched(double[] output) {


        double sum = 0.0;
        for (int i = 1; i < output.length; i++) {
            sum += Math.exp(output[i]);
        }

        for (int i = 1; i < output.length; i++) {
            output[i] = Math.exp(output[i]) / sum;
        }
    }
}
