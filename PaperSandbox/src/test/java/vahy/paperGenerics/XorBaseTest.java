package vahy.paperGenerics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;
import vahy.timer.SimpleTimer;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.SplittableRandom;

public class XorBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(XorBaseTest.class.getName());

    @Test
    public void xorTrainingTest() {
        SplittableRandom random = new SplittableRandom(0);

        int inputDim = 2;
        int outputDim = 1;

        double[][] inputData = { {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        double[][] targetData =  { {-1}, {1}, {1}, {-1}};

        int trainingIterations_IN_MODEL = 1;
        int trainingIterations_OUTSIDE_MODEL = 1000;
        int batchSize = 1;

        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python";
        var modelPath = Paths.get(XorBaseTest.class.getClassLoader().getResource("tfModelPrototypes/XOR.py").getPath());

        try {
            var modelRepresentation = TFHelper.loadTensorFlowModel(modelPath, environmentPath, random.nextLong(), inputDim, outputDim, 0);
            try(TFModelImproved model = new TFModelImproved(inputDim, outputDim, batchSize, trainingIterations_IN_MODEL, 0.5, 0.0001, modelRepresentation, 1, random))
            {
                for (int i = 0; i < trainingIterations_OUTSIDE_MODEL; i++) {
                    trainingLoop(inputData, targetData, model);
                }
                double[][] output = model.predict(inputData);
                if(output[0][0] >= 0 || output[1][0] <= 0 || output[2][0] <= 0 || output[3][0] >= 0) {
                    Assertions.fail("Xor test failed");
                }
            }
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    public static void trainingLoop(double[][] inputData, double[][] targetData, TFModelImproved model) {
        SimpleTimer timer = new SimpleTimer();
        double[][] outputData = new double[inputData.length][];

        timer.startTimer();
        for (int j = 0; j < inputData.length; j++) {
            double[] prediction = model.predict(inputData[j]);
            outputData[j] = new double[prediction.length];
            System.arraycopy(prediction, 0, outputData[j], 0, targetData[0].length);
        }
        timer.stopTimer();
        logger.info("Predicting [{}] samples by one took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.info("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        timer.startTimer();
        double[][] outputData2 = model.predict(inputData);
        timer.stopTimer();
        logger.info("Predicting [{}] samples in batch took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.info("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        timer.startTimer();
        double[][] outputData3 = model.predict(inputData);
        timer.stopTimer();
        logger.info("Predicting [{}] samples in batch took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.info("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        checkPredictionDifference(targetData[0].length, outputData, outputData2);
        checkPredictionDifference(targetData[0].length, outputData2, outputData3);
        printFirstPredictions(outputData, 10);
        model.fit(inputData, targetData);
    }

    public static void printFirstPredictions(double[][] outputData, int predictionCount) {
        for (int j = 0; j < Math.min(predictionCount, outputData.length); j++) {
            logger.info("Prediction: [{}]", Arrays.toString(outputData[j]));
        }
    }

    public static void checkPredictionDifference(int outputDim, double[][] outputData, double[][] outputData2) {
        for (int j = 0; j < outputData.length; j++) {
            for (int k = 0; k < outputDim; k++) {
                if(Math.abs(outputData2[j][k] - outputData[j][k]) > Math.pow(10, -10)) {
                    throw new IllegalStateException("Predictions differ at index: [" + j + "], diff: [" + Arrays.toString(outputData[j]) + "] and [" +  Arrays.toString(outputData2[j]) + "]");
                }
            }
        }
    }

}
