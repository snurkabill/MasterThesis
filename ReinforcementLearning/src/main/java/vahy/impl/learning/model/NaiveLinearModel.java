package vahy.impl.learning.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.learning.model.SupervisedTrainableStateValueModel;
import vahy.api.model.reward.RewardFactory;
import vahy.api.model.reward.VectorialReward;
import vahy.impl.model.DoubleVectorialObservation;

import java.util.List;

public class NaiveLinearModel<TReward extends VectorialReward, TObservation extends DoubleVectorialObservation> implements SupervisedTrainableStateValueModel<TReward, TObservation> {

    // TODO: split linear model and observation/reward

    private static final Logger logger = LoggerFactory.getLogger(NaiveLinearModel.class);

    private int inputLength;
    private int outputLength;
    private double weightMatrix[][];
    private double biasArray[];
    private final RewardFactory<TReward> rewardFactory;
    private final double learningRate;

    public NaiveLinearModel(int inputLength,
                            int outputLength,
                            RewardFactory<TReward> rewardFactory,
                            double learningRate) {
        this.inputLength = inputLength;
        this.outputLength = outputLength;
        this.rewardFactory = rewardFactory;
        this.learningRate = learningRate;
        this.weightMatrix = new double[outputLength][];
        for (int i = 0; i < weightMatrix.length; i++) {
            this.weightMatrix[i] = new double[inputLength];
        }
        this.biasArray = new double[outputLength];
        logger.debug("Created [{}] with input size [{}] and outputs size [{}]", NaiveLinearModel.class.getName(), inputLength, outputLength);
    }

    @Override
    public void fit(List<List<TObservation>> inputMatrix, List<TReward> rewardList) {
        if(inputMatrix.size() != rewardList.size()) {
            throw new IllegalArgumentException(
                "Input matrix and reward list differs in common dimension. Input matrix dim: [" + inputMatrix.size() + "], reward list dim: [" + rewardList.size() + "]");
        }
        for (int i = 0; i < rewardList.size(); i++) {
            TReward evaluatedReward = approximate(inputMatrix.get(i));
            double[] predicted = evaluatedReward.getAsVector();
            double[] expected = rewardList.get(i).getAsVector();
            double[] input = getInput(inputMatrix.get(i));
            if(input.length != weightMatrix[0].length) {
                throw new IllegalStateException("SafetyCheck - input size is different than weightInputSize");
            }
            for (int j = 0; j < predicted.length; j++) {
                double diff = predicted[j] - expected[j];
                for (int k = 0; k < weightMatrix[0].length; k++) {
                    weightMatrix[j][k] = weightMatrix[j][k] - learningRate * diff * input[k];
                }
                biasArray[j] = biasArray[j] - learningRate * diff;
            }
        }
    }

    @Override
    public TReward approximate(List<TObservation> observationList) {
        double[] input = getInput(observationList);
        double[] output = new double[outputLength];
        for (int i = 0; i < biasArray.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < weightMatrix[0].length; j++) {
                sum += input[j] * weightMatrix[i][j];
            }
            sum += biasArray[i];
            output[i] = sum;
        }
        return rewardFactory.fromNumericVector(output);
    }

    private double[] getInput(List<TObservation> observationList) {
        if(observationList.isEmpty()) {
            throw new IllegalArgumentException("Observation list is empty");
        }
        int observationVectorLength = observationList.get(0).getObservedVector().length;
        if(inputLength != observationVectorLength * observationList.size()) {
            throw new IllegalArgumentException("Expected input length: [" + inputLength + "]. Actual length: [" + observationVectorLength + "]");
        }
        double[] input = new double[inputLength];
        for (int observationIndex = 0, k = 0; observationIndex < observationList.size(); observationIndex++) {
            for (int i = 0; i < observationList.get(0).getObservedVector().length; i++, k++) {
                input[k] = observationList.get(observationIndex).getObservedVector()[i];
            }
        }
        return input;
    }
}
