package vahy.AlphaGo.reinforcement.learn;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.learning.model.SupervisedTrainableModel;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class AlphaGoDl4jModel implements SupervisedTrainableModel {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoDl4jModel.class);

    public static final String LAST_HIDDEN_LAYER = "lastHiddenLayer";
    public static final String POLICY_NAME = "policy";
    public static final String Q_VALUE_NAME = "QValue";
    public static final String INPUT_NAME = "input";

    public static final int FIRST_HIDDEN_SIZE = 20;
    public static final int SECOND_HIDDEN_SIZE = 20;
    public static final int THIRD_HIDDEN_SIZE = 20;

    public static final String FIRST_HIDDEN_NAME = "FIRST_HIDDEN";
    public static final String SECOND_HIDDEN_NAME = "SECOND_HIDDEN";
    public static final String THIRD_HIDDEN_NAME = "THIRD_HIDDEN";

    private final int inputDimension;
    private final int outputDimension;
    private final List<Integer> hiddenLayerSizeList;
    private  long seed;

    private final double learningRate;
    private final int trainingEpochCount;

    private final ComputationGraph model;
    private final INDArray feedForwardInputArray;
    private final INDArray policyArray;
    private final INDArray qValueArray;
    private long totalMillisCount;
    private long totalCallsCount;

    public AlphaGoDl4jModel(int inputDimension, int outputDimension, List<Integer> hiddenLayerSizeList, long seed, double learningRate, int trainingEpochCount) {
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
        this.hiddenLayerSizeList = hiddenLayerSizeList;
        this.seed = seed;
        this.learningRate = learningRate;
        this.trainingEpochCount = trainingEpochCount;
        this.model = initModel();
        this.feedForwardInputArray = Nd4j.create(new int[] {inputDimension});
        this.policyArray = Nd4j.create(new int[] {outputDimension - 1});
        this.qValueArray = Nd4j.create(new int[] {1});
    }

    public ComputationGraph initModel() {
        ComputationGraphConfiguration.GraphBuilder graph = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            // .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
            .updater(Adam.builder().learningRate(learningRate).build())
            .weightInit(WeightInit.NORMAL)
            .miniBatch(true)
            .trainingWorkspaceMode(WorkspaceMode.ENABLED)
            .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
//            .dropOut(0.2)
            .graphBuilder();



        graph
            .addInputs(INPUT_NAME) // .setInputTypes(InputType.feedForward(inputDimension))
            .addLayer(
                FIRST_HIDDEN_NAME,
                new DenseLayer.Builder()
                    .nIn(inputDimension)
                    .nOut(FIRST_HIDDEN_SIZE)
                    .activation(Activation.RELU)
                    .hasBias(true)
                    .build(),
                INPUT_NAME)
            .addLayer(
                SECOND_HIDDEN_NAME,
                new DenseLayer.Builder()
                    .nIn(FIRST_HIDDEN_SIZE)
                    .nOut(SECOND_HIDDEN_SIZE)
                    .activation(Activation.RELU)
                    .hasBias(true)
                    .build(),
                FIRST_HIDDEN_NAME)
//            .addLayer(
//                THIRD_HIDDEN_NAME,
//                new DenseLayer.Builder()
//                    .nIn(SECOND_HIDDEN_SIZE)
//                    .nOut(THIRD_HIDDEN_SIZE)
//                    .activation(Activation.RELU)
//                    .hasBias(true)
//                    .build(),
//                SECOND_HIDDEN_NAME)
            .addLayer(
            POLICY_NAME,
            new OutputLayer.Builder()
                .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(SECOND_HIDDEN_SIZE)
//                .nIn(inputDimension)
                .nOut(outputDimension - 1)
                .activation(Activation.SOFTMAX)
                .weightInit(WeightInit.ZERO)
                .hasBias(true)
                .build(),
            // LAST_HIDDEN_LAYER
                SECOND_HIDDEN_NAME)
//            INPUT_NAME)
            .addLayer(
                Q_VALUE_NAME,
                new OutputLayer.Builder()
                    .lossFunction(LossFunctions.LossFunction.MSE)
                    .activation(Activation.IDENTITY)
                    .weightInit(WeightInit.ZERO)
                    .hasBias(true)
                    .nIn(SECOND_HIDDEN_SIZE)
//                    .nIn(inputDimension)
                    .nOut(1)
                    .build(),
                // LAST_HIDDEN_LAYER
                SECOND_HIDDEN_NAME)
//            INPUT_NAME)
            .setOutputs(POLICY_NAME, Q_VALUE_NAME)
            .backprop(true)
            .pretrain(false);


//        String prevLayer = INPUT_NAME;
//
//        for (int i = 0; i < hiddenLayerSizeList.size(); i++) {
//            String layerName = i == hiddenLayerSizeList.size() - 1 ? LAST_HIDDEN_LAYER : "Layer" + i;
//            graph.addLayer(layerName, new DenseLayer.Builder().nIn(i == 0 ? inputDimension : hiddenLayerSizeList.get(i - 1)).nOut(hiddenLayerSizeList.get(i)).hasBias(true).build(), prevLayer);
//            prevLayer = layerName;
//        }

        ComputationGraphConfiguration conf = graph.build();
        ComputationGraph model = new ComputationGraph(conf);
        model.init();
        return model;
    }

    @Override
    public void fit(double[][] input, double[][] target) {
        MultiDataSetIterator iterator = new MultiDataSetIterator() {

            int cursor = 0;
            int order[] = IntStream.range(0, target.length).toArray();


            double[] policyTarget = new double[target[0].length - 1];
            double[] qTarget = new double[1];


            @Override
            public MultiDataSet next(int num) {
                throw new UnsupportedOperationException("Not supported for this iterator");
            }

            @Override
            public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
                throw new UnsupportedOperationException("Not supported for this iterator");
            }

            @Override
            public MultiDataSetPreProcessor getPreProcessor() {
                throw new UnsupportedOperationException("Not supported for this iterator");
            }

            @Override
            public boolean resetSupported() {
                return true;
            }

            @Override
            public boolean asyncSupported() {
                return false;
            }

            @Override
            public void reset() {
                cursor = 0;
                MathUtils.shuffleArray(order, seed++);
            }

            @Override
            public boolean hasNext() {
                return cursor < order.length;
            }

            @Override
            public MultiDataSet next() {
                int dataIndex = order[cursor];
                cursor++;

//                for (int i = 0; i < policyTarget.length; i++) {
//                    policyTarget[i] = target[dataIndex][i + 1];
//                }
//                qTarget[0] = target[dataIndex][0];
//                return new org.nd4j.linalg.dataset.MultiDataSet(
//                    new INDArray[] {Nd4j.create(input[dataIndex])},
//                    new INDArray[] {Nd4j.create(policyTarget), Nd4j.create(qTarget)}
//                );


                for (int i = 0; i < input[0].length; i++) {
                    feedForwardInputArray.put(0, i, input[dataIndex][i]);
                }
                for (int i = 0; i < policyTarget.length; i++) {
                    policyArray.put(0, i, target[dataIndex][i + 1]);
                }
                qValueArray.put(0, 0, target[dataIndex][0]);
                return new org.nd4j.linalg.dataset.MultiDataSet(
                    new INDArray[] {feedForwardInputArray},
                    new INDArray[] {policyArray, qValueArray}
                );
            }
        };

        iterator.reset();
        model.fit(iterator, trainingEpochCount);


    }

    @Override
    public double[] predict(double[] input) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < input.length; i++) {
            feedForwardInputArray.put(0, i, input[i]);
        }
        Map<String, INDArray> output =  model.feedForward(feedForwardInputArray, false);
        double[] probabilities = output.get(POLICY_NAME).toDoubleVector();
        double[] qValue = output.get(Q_VALUE_NAME).toDoubleVector();
        long end = System.currentTimeMillis();
        totalMillisCount += end - start;
        totalCallsCount++;

        if(totalCallsCount % 1000 == 0) {
            logger.info("Evaluation of single vector took: [{}) ms", totalMillisCount / (double) totalCallsCount);
        }


        if(qValue.length != 1) {
            throw new IllegalStateException("Qvalue Vector cannot have length different from 1");
        }
        if(probabilities.length + qValue.length != outputDimension) {
            throw new IllegalStateException("Total output length differs from expected output length");
        }
        double[] outputVector = new double[outputDimension];
        outputVector[0] = qValue[0];
        System.arraycopy(probabilities, 0, outputVector, 1, outputVector.length - 1);
        return outputVector;
    }

    @Override
    public double[][] predict(double[][] input) {
        Map<String, INDArray> output =  model.feedForward(Nd4j.create(input), false);
        double[][] probabilities = output.get(POLICY_NAME).toDoubleMatrix();
        double[][] qValue = output.get(Q_VALUE_NAME).toDoubleMatrix();
        if(qValue[0].length != 1) {
            throw new IllegalStateException("Qvalue Vector cannot have length different from 1");
        }
        if(probabilities[0].length + qValue[0].length != outputDimension) {
            throw new IllegalStateException("Total output length differs from expected output length");
        }
        double[][] outputMatrix = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            outputMatrix[i] = new double[outputDimension];
            outputMatrix[i][0] = qValue[i][0];
            System.arraycopy(probabilities[i], 0, outputMatrix[i], 1, probabilities.length - 1);
        }
        return outputMatrix;
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public int getOutputDimension() {
        return outputDimension;
    }
}
