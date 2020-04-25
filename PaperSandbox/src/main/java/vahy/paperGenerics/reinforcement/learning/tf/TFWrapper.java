package vahy.paperGenerics.reinforcement.learning.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.io.Closeable;
import java.nio.DoubleBuffer;
import java.util.List;

public class TFWrapper implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TFWrapper.class.getName());

    private final int inputDimension;
    private final int outputDimension;
    private final Session sess;

    private final double[] singleOutputArray;
    private final DoubleBuffer singleInputDoubleBuffer;
    private final DoubleBuffer singleOutputDoubleBuffer;

    private final long[] singleInputShape;
    private final long[] batchedInputShape;

    private Tensor<Double> inferenceKeepProbability = Tensors.create(1.0);

    public TFWrapper(int inputDimension, int outputDimension, Session sess) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;

        this.singleOutputArray = new double[outputDimension];
        this.singleOutputDoubleBuffer = DoubleBuffer.wrap(singleOutputArray);
        this.singleInputDoubleBuffer = DoubleBuffer.allocate(inputDimension);
        this.singleInputShape = new long[] {1, inputDimension};
        this.batchedInputShape = new long[] {0, inputDimension};

        this.sess = sess;
        logger.info("Initialized model based on TensorFlow backend.");
        logger.debug("Model with input dimension: [{}] and output dimension: [{}].", inputDimension, outputDimension);
    }

    public double[] predict(double[] input) {
        singleInputDoubleBuffer.position(0);
        singleInputDoubleBuffer.put(input);
        singleInputDoubleBuffer.flip();
        List<Tensor<?>> tensors = evaluateTensor(singleInputShape, singleInputDoubleBuffer);
        var output = tensors.get(0);
        output.writeTo(singleOutputDoubleBuffer);
        singleOutputDoubleBuffer.position(0);
        output.close();
        return singleOutputArray;
    }

    public double[][] predict(double[][] input) {
        DoubleBuffer doubleBuffer = DoubleBuffer.allocate(input.length * inputDimension);
        for (double[] doubles : input) {
            doubleBuffer.put(doubles);
        }
        doubleBuffer.position(0);
        batchedInputShape[0] = input.length;
        List<Tensor<?>> tensors = evaluateTensor(batchedInputShape, doubleBuffer);
        double[][] outputMatrix = new double[input.length][];
        for (int i = 0; i < outputMatrix.length; i++) {
            outputMatrix[i] = new double[outputDimension];
        }
        var output = tensors.get(0);
        output.copyTo(outputMatrix);
        output.close();
        return outputMatrix;
    }

    private List<Tensor<?>> evaluateTensor(long[] singleInputShape, DoubleBuffer singleInputDoubleBuffer) {
        var inputTensor = Tensor.create(singleInputShape, singleInputDoubleBuffer);
        List<Tensor<?>> tensors = sess
            .runner()
            .feed("input_node", inputTensor)
            .feed("keep_prob_node", inferenceKeepProbability)
            .fetch("prediction_node", 0)
            .run();
        inputTensor.close();
        if (tensors.size() != 1) {
            throw new IllegalStateException("There should be only one output tensor.");
        }
        return tensors;
    }

    @Override
    public void close() {
        logger.trace("Finalizing TF model resources");
        sess.close();
        inferenceKeepProbability.close();
        logger.debug("TF resources closed");
    }
}
