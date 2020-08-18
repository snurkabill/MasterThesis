package vahy.tensorflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.DoubleDataBuffer;
import org.tensorflow.types.TFloat64;

import java.io.Closeable;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.List;

public class TFWrapper implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TFWrapper.class.getName());

    private final int inputDimension;
    private final int outputDimension;
    private final Session sess;

    private final Shape singleInputShape;
    private final long[] batchedInputShape;

    private final Tensor<TFloat64> inferenceKeepProbability = TFloat64.scalarOf(1.0);

    public TFWrapper(int inputDimension, int outputDimension, Session sess) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;

        this.singleInputShape = Shape.of(1, inputDimension);
        this.batchedInputShape = new long[] {0, inputDimension};

        this.sess = sess;
        logger.info("Initialized model based on TensorFlow backend.");
        logger.debug("Model with input dimension: [{}] and output dimension: [{}].", inputDimension, outputDimension);
    }

    public double[] predict(double[] input) {
        var input2 = Arrays.copyOf(input, input.length);
        Tensor<TFloat64> output = evaluateTensor(singleInputShape, DataBuffers.of(input2));
        var prediction = new double[outputDimension];
        output.rawData().asDoubles().read(prediction);
        output.close();
        return prediction;
    }

    public double[][] predict(double[][] input) {
        DoubleBuffer doubleBuffer = DoubleBuffer.allocate(input.length * inputDimension);
        for (double[] doubles : input) {
            doubleBuffer.put(doubles);
        }
        doubleBuffer.position(0);
        batchedInputShape[0] = input.length;
        Tensor<?> output = evaluateTensor(Shape.of(batchedInputShape), DataBuffers.of(doubleBuffer));
        DoubleDataBuffer outputDoubleBuffer = output.rawData().asDoubles();
        var oneDArray = new double[input.length * outputDimension];
        outputDoubleBuffer.read(oneDArray);
        double[][] outputMatrix = new double[input.length][];
        for (int i = 0; i < outputMatrix.length; i++) {
            outputMatrix[i] = new double[outputDimension];
            System.arraycopy(oneDArray, i * outputDimension, outputMatrix[i], 0, outputDimension);
        }
        output.close();
        return outputMatrix;
    }

    private Tensor<TFloat64> evaluateTensor(Shape singleInputShape, DoubleDataBuffer singleInputDoubleBuffer) {
        Tensor<TFloat64> inputTensor = TFloat64.tensorOf(singleInputShape, singleInputDoubleBuffer);
        List<Tensor<?>> tensors = sess
            .runner()
            .feed("input_node", inputTensor)
            .feed("keep_prob_node", inferenceKeepProbability)
            .fetch("prediction_node", 0)
            .run();
        inputTensor.close();
        if (tensors.size() != 1) {
            throw new IllegalStateException("There is expected only one output tensor in this scenario. If multiple tensors present on output, different method should be written to handle it. Got tensors: [" + tensors.size() + "]");
        }
        return tensors.get(0).expect(TFloat64.DTYPE);
    }

    @Override
    public void close() {
        logger.trace("Finalizing TF model resources");
        sess.close();
        inferenceKeepProbability.close();
        logger.debug("TF resources closed");
    }
}
