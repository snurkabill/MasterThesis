package vahy.config;

import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.api.learning.ApproximatorType;

import java.util.function.Supplier;

public class AlgorithmConfigBuilder {

    // MCTS
    private double cpuctParameter;

    private TreeUpdateConditionFactory treeUpdateConditionFactory;

    // REINFORCEMENT
    private double discountFactor;
    private int batchEpisodeCount;
    private int replayBufferSize;
    private int stageCount;

    private Supplier<Double> explorationConstantSupplier;
    private Supplier<Double> temperatureSupplier;
    private Supplier<Double> riskSupplier;

    private DataAggregationAlgorithm dataAggregationAlgorithm;
    private ApproximatorType approximatorType;
    private EvaluatorType evaluatorType;
    private SelectorType selectorType;

    // NN
    private int trainingBatchSize;
    private int trainingEpochCount;

    private double learningRate;

    // PAPER
    private double globalRiskAllowed;
    private InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private FlowOptimizerType flowOptimizerType;
    private SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow;
    private SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow;

    private int batchedEvaluationSize;
    private NoiseStrategy noiseStrategy = NoiseStrategy.NOISY_03_04;

    private String creatingScriptName;


    public AlgorithmConfigBuilder cpuctParameter(double cpuctParameter) {
        this.cpuctParameter = cpuctParameter;
        return this;
    }

    public AlgorithmConfigBuilder discountFactor(double discountFactor) {
        this.discountFactor = discountFactor; return this;
    }

    public AlgorithmConfigBuilder batchEpisodeCount(int batchEpisodeCount) {
        this.batchEpisodeCount = batchEpisodeCount; return this;
    }

    public AlgorithmConfigBuilder replayBufferSize(int replayBufferSize) {
        this.replayBufferSize = replayBufferSize; return this;
    }

    public AlgorithmConfigBuilder stageCount(int stageCount) {
        this.stageCount = stageCount; return this;
    }

    public AlgorithmConfigBuilder explorationConstantSupplier(Supplier<Double> explorationConstantSupplier) {
        this.explorationConstantSupplier = explorationConstantSupplier; return this;
    }

    public AlgorithmConfigBuilder temperatureSupplier(Supplier<Double> temperatureSupplier) {
        this.temperatureSupplier = temperatureSupplier; return this;
    }

    public AlgorithmConfigBuilder riskSupplier(Supplier<Double> riskSupplier) {
        this.riskSupplier = riskSupplier; return this;
    }

    public AlgorithmConfigBuilder trainerAlgorithm(DataAggregationAlgorithm dataAggregationAlgorithm) {
        this.dataAggregationAlgorithm = dataAggregationAlgorithm; return this;
    }

    public AlgorithmConfigBuilder approximatorType(ApproximatorType approximatorType) {
        this.approximatorType = approximatorType; return this;
    }

    public AlgorithmConfigBuilder evaluatorType(EvaluatorType evaluatorType) {
        this.evaluatorType = evaluatorType; return this;
    }

    public AlgorithmConfigBuilder trainingBatchSize(int trainingBatchSize) {
        this.trainingBatchSize = trainingBatchSize; return this;
    }

    public AlgorithmConfigBuilder trainingEpochCount(int trainingEpochCount) {
        this.trainingEpochCount = trainingEpochCount; return this;
    }

    public AlgorithmConfigBuilder learningRate(double learningRate) {
        this.learningRate = learningRate; return this;
    }

    public AlgorithmConfigBuilder globalRiskAllowed(double globalRiskAllowed) {
        this.globalRiskAllowed = globalRiskAllowed; return this;
    }

    public AlgorithmConfigBuilder treeUpdateConditionFactory(TreeUpdateConditionFactory treeUpdateConditionFactory) {
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        return this;
    }

    public AlgorithmConfigBuilder selectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
        return this;
    }

    public AlgorithmConfigBuilder setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy inferenceExistingFlowStrategy) {
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        return this;
    }

    public AlgorithmConfigBuilder setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy) {
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        return this;
    }

    public AlgorithmConfigBuilder setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy explorationExistingFlowStrategy) {
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        return this;
    }

    public AlgorithmConfigBuilder setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy) {
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        return this;
    }

    public AlgorithmConfigBuilder setFlowOptimizerType(FlowOptimizerType flowOptimizerType) {
        this.flowOptimizerType = flowOptimizerType;
        return this;
    }

    public AlgorithmConfigBuilder setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow) {
        this.subTreeRiskCalculatorTypeForKnownFlow = subTreeRiskCalculatorTypeForKnownFlow;
        return this;
    }

    public AlgorithmConfigBuilder setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow) {
        this.subTreeRiskCalculatorTypeForUnknownFlow = subTreeRiskCalculatorTypeForUnknownFlow;
        return this;
    }

    public AlgorithmConfigBuilder setBatchedEvaluationSize(int batchedEvaluationSize) {
        this.batchedEvaluationSize = batchedEvaluationSize;
        return this;
    }

    public AlgorithmConfigBuilder setCreatingScriptName(String creatingScriptName) {
        this.creatingScriptName = creatingScriptName;
        return this;
    }

    public AlgorithmConfigBuilder setNoiseStrategy(NoiseStrategy noiseStrategy) {
        this.noiseStrategy = noiseStrategy;
        return this;
    }

    public PaperAlgorithmConfig buildAlgorithmConfig() {
        return new PaperAlgorithmConfig(
            cpuctParameter,
            treeUpdateConditionFactory,
            discountFactor,
            batchEpisodeCount,
            replayBufferSize,
            stageCount,
            explorationConstantSupplier,
            temperatureSupplier,
            riskSupplier,
            dataAggregationAlgorithm,
            approximatorType,
            evaluatorType,
            selectorType,
            trainingBatchSize,
            trainingEpochCount,
            learningRate,
            globalRiskAllowed,
            inferenceExistingFlowStrategy,
            inferenceNonExistingFlowStrategy,
            explorationExistingFlowStrategy,
            explorationNonExistingFlowStrategy,
            flowOptimizerType,
            subTreeRiskCalculatorTypeForKnownFlow,
            subTreeRiskCalculatorTypeForUnknownFlow,
            batchedEvaluationSize,
            creatingScriptName,
            noiseStrategy);
    }

}
