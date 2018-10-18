package vahy.AlphaGo.reinforcement.learn.tf;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import vahy.AlphaGo.tree.AlphaGoNodeEvaluator;
import vahy.api.learning.model.SupervisedTrainableModel;

import java.io.File;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TFModel implements SupervisedTrainableModel {


    private final int inputDimension;
    private final int outputDimension;
    private final int trainingIterations;
    private final SplittableRandom random;
    private final Graph graph = new Graph();
    private final Session sess;


    public TFModel(int inputDimension, int outputDimension, int trainingIterations, File graphFile, SplittableRandom random) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
        this.trainingIterations = trainingIterations;
        this.random = random;
        this.sess = new Session(this.graph);
        try {
            this.graph.importGraphDef(Files.readAllBytes(Paths.get(graphFile.getAbsolutePath())));
        } catch (IOException e) {
            throw new IllegalArgumentException("Tf model handling crashed. TODO: quite general exception", e);
        }
        this.sess.runner().addTarget("init").run();
        printVariables(sess);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        sess.close();
    }

    @Override
    public void fit(double[][] input, double[][] target) {

        double[] qTarget = new double[input.length];
        double[] rTarget = new double[input.length];
        double[][] policyTarget = new double[input.length][];
        int[] order = IntStream.range(0, input.length).toArray();

        for (int i = 0; i < target.length; i++) {
            qTarget[i] = target[i][AlphaGoNodeEvaluator.Q_VALUE_INDEX];
            rTarget[i] = target[i][AlphaGoNodeEvaluator.RISK_VALUE_INDEX];
            policyTarget[i] = new double[target[0].length - AlphaGoNodeEvaluator.POLICY_START_INDEX];
            System.arraycopy(
                target[i],
                AlphaGoNodeEvaluator.POLICY_START_INDEX,
                policyTarget[i],
                0,
                target[0].length - AlphaGoNodeEvaluator.POLICY_START_INDEX);
        }
        for (int i = 0; i < trainingIterations * target.length; i++) {
            int index = random.nextInt(input.length);
            try (
                Tensor<Double> tfInput = Tensors.create(new double[][] {input[order[index]]});
                Tensor<Double> tfQTarget = Tensors.create(new double[] {qTarget[order[index]]});
                Tensor<Double> tfRTarget = Tensors.create(new double[] {rTarget[order[index]]});
                Tensor<Double> tfPolicyTarget = Tensors.create(new double[][] {policyTarget[order[index]]});
                )
            {
                sess
                    .runner()
                    .feed("input_node", tfInput)
                    .feed("Q_target_node", tfQTarget)
                    .feed("Risk_target_node", tfRTarget)
                    .feed("Policy_target_node", tfPolicyTarget)
                    .addTarget("train_node")
                    .run();
            }
        }

    }

    public static void shuffleArray(int[] array, Random rng) {
        for(int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            int temp = array[j];
            array[j] = array[i];
            array[i] = temp;
        }

    }

    @Override
    public double[] predict(double[] input) {
        double[][] matrix = new double[1][];
        matrix[0] = input;
        try (Tensor<Double> tfInput = Tensors.create(matrix)) {
            Tensor<Double> output = sess
                .runner()
                .feed("input_node", tfInput)
                .fetch("prediction_node_2")
                .run()
                .get(0)
                .expect(Double.class);
            double[] outputVector = new double[outputDimension];
            DoubleBuffer doubleBuffer = DoubleBuffer.wrap(outputVector);
            output.writeTo(doubleBuffer);
            output.close();  // needed?
            return outputVector;
        }
    }

    @Override
    public double[][] predict(double[][] input) {
        try (Tensor<Double> tfInput = Tensors.create(input)) {
            List<Tensor<Double>> output = sess
                .runner()
                .feed("input_node", tfInput)
                .fetch("prediction_node_2")
                .run()
                .stream()
                .map(x -> x.expect(Double.class))
                .collect(Collectors.toList());
            double[][] outputMatrix = new double[input.length][];
            for (int i = 0; i < outputMatrix.length; i++) {
                double[] outputVector = new double[outputDimension];
                DoubleBuffer doubleBuffer = DoubleBuffer.wrap(outputVector);
                output.get(i).writeTo(doubleBuffer);
                outputMatrix[i] = outputVector;
            }
            output.forEach(Tensor::close); // needed?
            return outputMatrix;
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

    private static void printVariables(Session sess) {
//        List<Tensor<?>> values = sess.runner().fetch("q_node/BiasAdd:0").run();
//        System.out.printf("W = %f\tb = %f\n", values.get(0).floatValue(), values.get(1).floatValue());
//        for (Tensor<?> t : values) {
//            t.close();
//        }
    }
}


