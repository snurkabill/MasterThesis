package vahy.thirdpartyprototype.TF.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paperGenerics.reinforcement.learning.tf.TFModelImproved;
import vahy.utils.RandomDistributionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrencyTesting {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyTesting.class.getName());

    public static void main(String[] args) throws IOException {
        runTest(ConcurrencyTesting.class.getClassLoader().getResourceAsStream("tfModel/graph_FastTF.pb").readAllBytes(), false);
        runTest(ConcurrencyTesting.class.getClassLoader().getResourceAsStream("tfModel/graph_FastTF_probs.pb").readAllBytes(), true);
    }



    public static void runTest(byte[] modelRepresentation, boolean predictProbability) {
        SplittableRandom random = new SplittableRandom(987412365);

        int instanceCount = 10_000;
        int batchSize = 1024;
        int inputDim = 20;
        int outputDim = 5;
        int trainingIterations = 10;

        double[][] inputData = new double[instanceCount][];
        double[][] targetData = new double[instanceCount][];

        for (int i = 0; i < instanceCount; i++) {
            double[] input = new double[inputDim];
            double[] target = new double[outputDim];
            for (int j = 0; j < inputDim; j++) {
                input[j] = random.nextDouble() - 0.5;
            }
            for (int j = 0; j < outputDim; j++) {
                target[j] = random.nextDouble();
            }
            inputData[i] = input;
            targetData[i] = target;
        }

        if(predictProbability) {
            for (int i = 0; i < targetData.length; i++) {
                RandomDistributionUtils.applySoftmax(targetData[i]);
            }
        }

        int workerCount = 10;
        int poolSize = 10;
        int workerBatchSize = 100;
        if(instanceCount % (workerCount * workerBatchSize) != 0) {
            throw new IllegalStateException("wrong dims");
        }
        int batchesPerWorker = instanceCount / (workerCount * workerBatchSize);

        try(TFModelImproved model = new TFModelImproved(inputDim, outputDim, batchSize, trainingIterations, 0.5, 0.0001, modelRepresentation, poolSize, random))
        {
            for (int i = 0; i < 20; i++) {
                var start = System.nanoTime();
                double[][] outputData = new double[inputData.length][];
                for (int j = 0; j < inputData.length; j++) {
                    double[] prediction = model.predict(inputData[j]);
                    outputData[j] = new double[prediction.length];
                    System.arraycopy(prediction, 0, outputData[j], 0, targetData[0].length);
                }
                var end = System.nanoTime();
                logger.info("Singlethreaded us per sample: [{}]", (end - start) / (inputData.length * 1000.0));

                if(predictProbability) {
                    testProbabilityDistribution(targetData, outputData);
                }

                ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

                var workerList = new ArrayList<Worker>(workerCount);
                for (int j = 0; j < workerCount; j++) {
                    workerList.add(new Worker(model, j * batchesPerWorker * workerBatchSize, (j + 1) * batchesPerWorker * workerBatchSize, inputData, workerBatchSize));
                }

                start = System.nanoTime();
                var future = executorService.invokeAll(workerList);
                executorService.shutdown();
                executorService.awaitTermination(10, TimeUnit.SECONDS);



                var value = joinBatches(future.stream().map(x -> {
                    try {
                        return x.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList()));
                end = System.nanoTime();
                logger.info("Parallel us per sample: [{}]", (end - start) / (inputData.length * 1000.0));

                checkPredictionDifference(targetData[0].length, outputData, value);

                printFirstPredictions(outputData, 10);

                if(i < 5) {
                    model.fit(inputData, targetData);
                }
                logger.info("Index: [{}]", i);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testProbabilityDistribution(double[][] targetData, double[][] outputData) {

        if(targetData.length != outputData.length) {
            throw new IllegalArgumentException("Different lengths");
        }
        for (int i = 0; i < targetData.length; i++) {
            if(!RandomDistributionUtils.isDistribution(targetData[i], RandomDistributionUtils.TOLERANCE)) {
                throw new IllegalStateException("Target data are not distribution: [" + Arrays.toString(targetData[i]) + "] on index: [" + i + "]");
            }
            if(!RandomDistributionUtils.isDistribution(outputData[i], RandomDistributionUtils.TOLERANCE)) {
                throw new IllegalStateException("Output data are not distribution: [" + Arrays.toString(outputData[i]) + "] on index: [" + i + "]");
            }
        }

    }

    public static void printFirstPredictions(double[][] outputData, int predictionCount) {
        for (int j = 0; j < (Math.min(predictionCount, outputData.length)); j++) {
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

    private static double[][] joinBatches(List<double[][]> batches) {
        var final_predictions = new double[batches.size() * batches.get(0).length][];
        int counter = 0;
        for (int i = 0; i < batches.size(); i++) {
            for (int j = 0; j < batches.get(0).length; j++) {
                final_predictions[counter] = batches.get(i)[j];
                counter++;
            }
        }
        return  final_predictions;
    }

}
