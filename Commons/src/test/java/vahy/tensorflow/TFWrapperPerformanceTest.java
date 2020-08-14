package vahy.tensorflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.SplittableRandom;

public class TFWrapperPerformanceTest {

    public static final Logger logger = LoggerFactory.getLogger(TFWrapperPerformanceTest.class);

    @Test
    public void latencyTest() {
        SplittableRandom random = new SplittableRandom(0);

        int inputDim = 2;
        int outputDim = 1;

        double[][] inputTrainData = { {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        double[][] targetTrainData =  { {-1}, {-1}, {-1}, {1}};

        int trainingIterations_IN_MODEL = 1;
        int trainingIterations_OUTSIDE_MODEL = 1000;
        int batchSize = 1;

        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python";
//        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python";
        var modelPath = Paths.get(ConcurrencyTesting.class.getClassLoader().getResource("tfModelPrototypes/AND.py").getPath());

        try {
            var modelRepresentation = TFHelper.loadTensorFlowModel(modelPath, environmentPath, random.nextLong(), inputDim, outputDim, 0);
            try(TFModelImproved model = new TFModelImproved(inputDim, outputDim, batchSize, trainingIterations_IN_MODEL, 0.7, 0.0001, modelRepresentation, 1, random))
            {
                for (int i = 0; i < trainingIterations_OUTSIDE_MODEL; i++) {
                    TFHelper.trainingLoop(inputTrainData, targetTrainData, model);
                }
                double[][] output = model.predict(inputTrainData);
                if(output[0][0] >= 0 || output[1][0] >= 0 || output[2][0] >= 0 || output[3][0] <= 0) {
                    Assertions.fail("AND test failed");
                }

                int instanceCount = 10_000;

                double[][] inputData = new double[instanceCount][];

                for (int i = 0; i < instanceCount; i++) {
                    double[] input = new double[inputDim];
                    for (int j = 0; j < inputDim; j++) {
                        input[j] = random.nextDouble() - 0.5;
                    }
                    inputData[i] = input;
                }

                var trials = 100;
                long start = System.currentTimeMillis();
                for (int i = 0; i < trials; i++) {
                    for (int j = 0; j < instanceCount; j++) {
                        model.predict(inputData[j]);
                    }
                }
                long end = System.currentTimeMillis();
                var microseconds = (end - start) * 1000.0;
                logger.info("Time prediction one by one total: [{}] us", microseconds / (double) trials);
                logger.info("Time prediction one by one per instance: [{}] us", microseconds / (double) (instanceCount * trials));
                logger.info("Time per call: [{}] us", microseconds / (double) (instanceCount * trials));

                start = System.currentTimeMillis();
                for (int i = 0; i < trials; i++) {
                    model.predict(inputData);
                }
                end = System.currentTimeMillis();
                microseconds = (end - start) * 1000.0;
                logger.info("Time per batched prediction [{}] us", microseconds / (double) trials);
                logger.info("Time per prediction in batch per sample: [{}] us", microseconds / (double) (instanceCount * trials));
                logger.info("Time per call: [{}] us", microseconds / (double) trials);
            }
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void throughputTest() {
        SplittableRandom random = new SplittableRandom(0);

        int inputDim = 512;
        int outputDim = 512;

        int instanceCount = 1000;

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

        int trainingIterations_IN_MODEL = 1;
        int trainingIterations_OUTSIDE_MODEL = 1;
        int batchSize = 1;

        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python";
//        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python";
        var modelPath = Paths.get(ConcurrencyTesting.class.getClassLoader().getResource("tfModelPrototypes/HUGE_MODEL.py").getPath());

        try {
            var modelRepresentation = TFHelper.loadTensorFlowModel(modelPath, environmentPath, random.nextLong(), inputDim, outputDim, 0);
            try(TFModelImproved model = new TFModelImproved(inputDim, outputDim, batchSize, trainingIterations_IN_MODEL, 0.7, 0.0001, modelRepresentation, 1, random))
            {
                for (int i = 0; i < trainingIterations_OUTSIDE_MODEL; i++) {
                    TFHelper.trainingLoop(inputData, targetData, model);
                }

                var trials = 100;
                long start = System.currentTimeMillis();
                for (int i = 0; i < trials; i++) {
                    for (int j = 0; j < instanceCount; j++) {
                        model.predict(inputData[j]);
                    }
                }
                long end = System.currentTimeMillis();
                var microseconds = (end - start) * 1000.0;
                logger.info("Time prediction one by one total: [{}] us", microseconds / (double) trials);
                logger.info("Time prediction one by one per instance: [{}] us", microseconds / (double) (instanceCount * trials));
                logger.info("Time per call: [{}] us", microseconds / (double) (instanceCount * trials));

                start = System.currentTimeMillis();
                for (int i = 0; i < trials; i++) {
                    model.predict(inputData);
                }
                end = System.currentTimeMillis();
                microseconds = (end - start) * 1000.0;
                logger.info("Time per batched prediction [{}] us", microseconds / (double) trials);
                logger.info("Time per prediction in batch per sample: [{}] us", microseconds / (double) (instanceCount * trials));
                logger.info("Time per call: [{}] us", microseconds / (double) trials);
            }
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }



}
