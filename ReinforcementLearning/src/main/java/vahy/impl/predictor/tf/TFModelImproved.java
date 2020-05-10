package vahy.impl.predictor.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.timer.SimpleTimer;

import java.nio.DoubleBuffer;
import java.util.SplittableRandom;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TFModelImproved implements SupervisedTrainableModel, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(TFModelImproved.class.getName());
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final BlockingQueue<TFWrapper> pool;

    private final SimpleTimer timer = new SimpleTimer();
    private final SplittableRandom random;
    private final int inputDimension;
    private final int outputDimension;
    private final int trainingIterations;
    private final int batchSize;
    private final double[][] trainInputBatch;
    private final double[][] trainTargetBatch;
    private final long[] trainInputShape;
    private final long[] trainTargetShape;

    private final Graph commonGraph;
    private final Session trainingSession;
    private final Tensor trainingKeepProbability;
    private final Tensor learningRate;

    private final DoubleBuffer inputDoubleBuffer;
    private final DoubleBuffer targetDoubleBuffer;


    public TFModelImproved(int inputDimension, int outputDimension, int batchSize, int trainingIterations, double keepProb, double learningRate, byte[] bytes, int poolSize, SplittableRandom random) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
        this.batchSize = batchSize;
        this.random = random;
        this.trainingIterations = trainingIterations;
        this.trainInputBatch = new double[batchSize][];
        this.trainTargetBatch = new double[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            trainInputBatch[i] = new double[inputDimension];
            trainTargetBatch[i] = new double[outputDimension];
        }
        this.trainInputShape = new long[] {batchSize, inputDimension};
        this.trainTargetShape = new long[] {batchSize, outputDimension};

        this.inputDoubleBuffer = DoubleBuffer.allocate(batchSize * inputDimension);
        this.targetDoubleBuffer = DoubleBuffer.allocate(batchSize * outputDimension);

        this.commonGraph = new Graph();
        this.commonGraph.importGraphDef(bytes);
        this.trainingSession = new Session(commonGraph);
        this.trainingSession.runner().addTarget("init").run();
        this.trainingKeepProbability = Tensor.create(keepProb);
        this.learningRate = Tensor.create(learningRate);

        this.pool = new ArrayBlockingQueue<>(poolSize, true);
        for (int i = 0; i < poolSize; i++) {
            this.pool.add(new TFWrapper(inputDimension, outputDimension, trainingSession));
        }
    }

    @Override
    public double[] predict(double[] input) {
        try {
            var tfWrapper = pool.take();
            double[] prediction = tfWrapper.predict(input);
            pool.add(tfWrapper);
            return prediction;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Model prediction was interrupted.", e);
        }
    }

    @Override
    public double[][] predict(double[][] input) {
        try {
            var tfWrapper = pool.take();
            double[][] prediction = tfWrapper.predict(input);
            pool.add(tfWrapper);
            return prediction;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Model prediction was interrupted.", e);
        }
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public int getOutputDimension() {
        return outputDimension;
    }

    @Override
    public void fit(double[][] input, double[][] target) {
        if(input.length != target.length) {
            throw new IllegalArgumentException("Input and target lengths differ");
        }
        if(DEBUG_ENABLED) {
            logger.debug("Partially fitting TF model on [{}] inputs with random.nextInt(): [{}]", input.length, random.nextInt());
        }
        timer.startTimer();
        int[] order = new int[input.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        for (int i = 0; i < trainingIterations; i++) {
            shuffleArray(order, random);
            for (int j = 0; j < (target.length / batchSize) + 1; j++) {
                inputDoubleBuffer.position(0);
                targetDoubleBuffer.position(0);
                fillBatch(j, order, input, target);
                for (double[] inputBatch : trainInputBatch) {
                    inputDoubleBuffer.put(inputBatch);
                }
                for (double[] targetBatch : trainTargetBatch) {
                    targetDoubleBuffer.put(targetBatch);
                }
                inputDoubleBuffer.position(0);
                targetDoubleBuffer.position(0);

                Tensor<Double> tfInput = Tensor.create(trainInputShape, inputDoubleBuffer);
                Tensor<Double> tfTarget = Tensor.create(trainTargetShape, targetDoubleBuffer);
                trainingSession
                    .runner()
                    .feed("input_node", tfInput)
                    .feed("target_node", tfTarget)
                    .feed("keep_prob_node", trainingKeepProbability)
                    .feed("learning_rate_node", learningRate)
                    .addTarget("optimize_node")
                    .run();
                tfInput.close();
                tfTarget.close();
            }
        }
        timer.stopTimer();
        if(DEBUG_ENABLED) {
            logger.debug("Training of [{}] inputs with minibatch size [{}] took [{}] milliseconds. Samples per sec: [{}], Ending with random.nextInt(): [{}]",
                input.length, batchSize, timer.getTotalTimeInMillis() / 1000.0, timer.samplesPerSec(input.length), random.nextInt());
        }
    }

    private void fillBatch(int batchesDone, int[] order, double[][] input, double[][] target) {
        if(TRACE_ENABLED) {
            logger.trace("Filling data batch. Already done batches: [{}]", batchesDone);
        }
        for (int i = 0; i < batchSize; i++) {
            int index = batchesDone * batchSize + i;
            if(index >= order.length) {
                break; // leaving part of batch from previous iteration.
            }
            System.arraycopy(input[order[index]], 0, trainInputBatch[i], 0, inputDimension);
            System.arraycopy(target[order[index]], 0, trainTargetBatch[i], 0, outputDimension);
        }
    }

    private static void shuffleArray(int[] array, SplittableRandom rng) {
        for(int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            int temp = array[j];
            array[j] = array[i];
            array[i] = temp;
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < pool.size(); i++) {
            try {
                pool.take().close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.trainingSession.close();
    }
}
