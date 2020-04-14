package vahy.config;

import vahy.api.experiment.ApproximatorConfig;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.util.function.Supplier;

public class AlgorithmConfigBuilder {

    private String algorithmId;

    // MCTS
    private double cpuctParameter;

    private TreeUpdateConditionFactory treeUpdateConditionFactory;

    // REINFORCEMENT
    private double discountFactor;
    private int batchEpisodeCount;
    private int stageCount;

    private Supplier<Double> explorationConstantSupplier;
    private Supplier<Double> temperatureSupplier;
    private Supplier<Double> riskSupplier;

    private EvaluatorType evaluatorType;
    private SelectorType selectorType;

    private ApproximatorConfig playerApproximatorConfig;
    private ApproximatorConfig opponentApproximatorConfig;

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

    public AlgorithmConfigBuilder algorithmId(String algorithmId) {
        this.algorithmId = algorithmId;
        return this;
    }

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

    public AlgorithmConfigBuilder evaluatorType(EvaluatorType evaluatorType) {
        this.evaluatorType = evaluatorType; return this;
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

    public AlgorithmConfigBuilder setNoiseStrategy(NoiseStrategy noiseStrategy) {
        this.noiseStrategy = noiseStrategy;
        return this;
    }

    public AlgorithmConfigBuilder setPlayerApproximatorConfig(ApproximatorConfig approximatorConfig) {
        this.playerApproximatorConfig = approximatorConfig;
        return this;
    }

    public AlgorithmConfigBuilder setOpponentApproximatorConfig(ApproximatorConfig approximatorConfig) {
        this.opponentApproximatorConfig = approximatorConfig;
        return this;
    }

    public PaperAlgorithmConfig buildAlgorithmConfig() {
        return new PaperAlgorithmConfig(
            algorithmId,
            cpuctParameter,
            treeUpdateConditionFactory,
            discountFactor,
            batchEpisodeCount,
            stageCount,
            explorationConstantSupplier,
            temperatureSupplier,
            riskSupplier,
            evaluatorType,
            selectorType,
            playerApproximatorConfig,
            opponentApproximatorConfig,
            globalRiskAllowed,
            inferenceExistingFlowStrategy,
            inferenceNonExistingFlowStrategy,
            explorationExistingFlowStrategy,
            explorationNonExistingFlowStrategy,
            flowOptimizerType,
            subTreeRiskCalculatorTypeForKnownFlow,
            subTreeRiskCalculatorTypeForUnknownFlow,
            batchedEvaluationSize,
            noiseStrategy);
    }

}
