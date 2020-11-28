package vahy.tensorflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.timer.SimpleTimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;

public class TFHelper {

    public static final Logger logger = LoggerFactory.getLogger(TFHelper.class.getName());

    private TFHelper() {
    }

    public static byte[] loadTensorFlowModel(Path scriptPath, String pythonVirtualEnvPath, long randomSeed, int inputCount, int valueOutputCount, int outputActionCount) throws IOException, InterruptedException {
        if(pythonVirtualEnvPath == null) {
            throw new IllegalStateException("Python virtualEnv path is null.");
        }
        var modelName = "tfModel_" + LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneOffset.UTC);
        modelName = modelName.replace(":", "_");
        Process process = Runtime.getRuntime().exec(pythonVirtualEnvPath
            + " " +
            scriptPath +
            " " +
            modelName +
            " " +
            inputCount +
            " " +
            valueOutputCount +
            " " +
            outputActionCount +
            " " +
            Paths.get("PythonScripts", "generated_models") +
            " " +
            (int) randomSeed);

        try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()))) {
            String line;
            String line2;

            while ((line = input.readLine()) != null) {
                logger.info(line);
            }
            while ((line2 = error.readLine()) != null) {
                logger.error(line2);
            }
        }
        var exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new IllegalStateException("Python process ended with non-zero exit value. Exit val: [" + exitValue + "]");
        }
        var dir = new File(Paths.get("PythonScripts", "generated_models").toString());
        Files.createDirectories(dir.toPath());
        return Files.readAllBytes(new File(dir, modelName + ".pb").toPath());
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
        logger.debug("Predicting [{}] samples by one took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.debug("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        timer.startTimer();
        double[][] outputData2 = model.predict(inputData);
        timer.stopTimer();
        logger.debug("Predicting [{}] samples in batch took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.debug("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        timer.startTimer();
        double[][] outputData3 = model.predict(inputData);
        timer.stopTimer();
        logger.debug("Predicting [{}] samples in batch took: [{}] ms. Per sample: [{}] ms. ", inputData.length, timer.getTotalTimeInNanos() / (1000.0 * 1000.0), timer.getTotalTimeInNanos() / (1000.0 * 1000.0 * inputData.length));
        logger.debug("Precise: [{}] nanos per sample", timer.getTotalTimeInNanos() / (double) inputData.length);

        checkPredictionDifference(targetData[0].length, outputData, outputData2);
        checkPredictionDifference(targetData[0].length, outputData2, outputData3);
//        printFirstPredictions(outputData, 10);
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
