package vahy.config;

import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;

import java.util.function.Supplier;

public class AlgorithmConfig {

    // MCTS
    private final double cpuctParameter;

    private final TreeUpdateConditionFactory treeUpdateConditionFactory;

    // REINFORCEMENT
    private final double discountFactor;
    private final int batchEpisodeCount;
    private final int replayBufferSize;
    private final int maximalStepCountBound;
    private final int stageCount;

    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    private final TrainerAlgorithm trainerAlgorithm;
    private final ApproximatorType approximatorType;
    private final EvaluatorType evaluatorType;
    private final SelectorType selectorType;

    // NN
    private final int trainingBatchSize;
    private final int trainingEpochCount;

    private final double learningRate;

    // PAPER
    private final double globalRiskAllowed;
    private final InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private final InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private final ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private final ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private final FlowOptimizerType flowOptimizerType;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow;

    public AlgorithmConfig(double cpuctParameter,
                           TreeUpdateConditionFactory treeUpdateConditionFactory,
                           double discountFactor,
                           int batchEpisodeCount,
                           int replayBufferSize,
                           int maximalStepCountBound,
                           int stageCount,
                           Supplier<Double> explorationConstantSupplier,
                           Supplier<Double> temperatureSupplier,
                           Supplier<Double> riskSupplier,
                           TrainerAlgorithm trainerAlgorithm,
                           ApproximatorType approximatorType,
                           EvaluatorType evaluatorType,
                           SelectorType selectorType,
                           int trainingBatchSize,
                           int trainingEpochCount,
                           double learningRate,
                           double globalRiskAllowed,
                           InferenceExistingFlowStrategy inferenceExistingFlowStrategy,
                           InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy,
                           ExplorationExistingFlowStrategy explorationExistingFlowStrategy,
                           ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy,
                           FlowOptimizerType flowOptimizerType,
                           SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow,
                           SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow) {

        this.cpuctParameter = cpuctParameter;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.discountFactor = discountFactor;
        this.batchEpisodeCount = batchEpisodeCount;
        this.replayBufferSize = replayBufferSize;
        this.maximalStepCountBound = maximalStepCountBound;
        this.stageCount = stageCount;
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.riskSupplier = riskSupplier;
        this.trainerAlgorithm = trainerAlgorithm;
        this.approximatorType = approximatorType;
        this.evaluatorType = evaluatorType;
        this.selectorType = selectorType;
        this.trainingBatchSize = trainingBatchSize;
        this.trainingEpochCount = trainingEpochCount;
        this.learningRate = learningRate;
        this.globalRiskAllowed = globalRiskAllowed;
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        this.flowOptimizerType = flowOptimizerType;
        this.subTreeRiskCalculatorTypeForKnownFlow = subTreeRiskCalculatorTypeForKnownFlow;
        this.subTreeRiskCalculatorTypeForUnknownFlow = subTreeRiskCalculatorTypeForUnknownFlow;
    }

    public double getCpuctParameter() {
        return cpuctParameter;
    }

    public TreeUpdateConditionFactory getTreeUpdateConditionFactory() {
        return treeUpdateConditionFactory;
    }

    public double getDiscountFactor() {
        return discountFactor;
    }

    public int getBatchEpisodeCount() {
        return batchEpisodeCount;
    }

    public int getReplayBufferSize() {
        return replayBufferSize;
    }

    public int getMaximalStepCountBound() {
        return maximalStepCountBound;
    }

    public int getStageCount() {
        return stageCount;
    }

    public Supplier<Double> getExplorationConstantSupplier() {
        return explorationConstantSupplier;
    }

    public Supplier<Double> getTemperatureSupplier() {
        return temperatureSupplier;
    }

    public Supplier<Double> getRiskSupplier() {
        return riskSupplier;
    }

    public TrainerAlgorithm getTrainerAlgorithm() {
        return trainerAlgorithm;
    }

    public ApproximatorType getApproximatorType() {
        return approximatorType;
    }

    public EvaluatorType getEvaluatorType() {
        return evaluatorType;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public int getTrainingBatchSize() {
        return trainingBatchSize;
    }

    public int getTrainingEpochCount() {
        return trainingEpochCount;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getGlobalRiskAllowed() {
        return globalRiskAllowed;
    }

    public InferenceExistingFlowStrategy getInferenceExistingFlowStrategy() {
        return inferenceExistingFlowStrategy;
    }

    public InferenceNonExistingFlowStrategy getInferenceNonExistingFlowStrategy() {
        return inferenceNonExistingFlowStrategy;
    }

    public ExplorationExistingFlowStrategy getExplorationExistingFlowStrategy() {
        return explorationExistingFlowStrategy;
    }

    public ExplorationNonExistingFlowStrategy getExplorationNonExistingFlowStrategy() {
        return explorationNonExistingFlowStrategy;
    }

    public FlowOptimizerType getFlowOptimizerType() {
        return flowOptimizerType;
    }

    public SubTreeRiskCalculatorType getSubTreeRiskCalculatorTypeForKnownFlow() {
        return subTreeRiskCalculatorTypeForKnownFlow;
    }

    public SubTreeRiskCalculatorType getSubTreeRiskCalculatorTypeForUnknownFlow() {
        return subTreeRiskCalculatorTypeForUnknownFlow;
    }

    @Override
    public String toString() {
        return "AlgorithmConfig{" +
            "cpuctParameter=" + cpuctParameter +
            ", treeUpdateConditionFactory=" + treeUpdateConditionFactory +
            ", discountFactor=" + discountFactor +
            ", batchEpisodeCount=" + batchEpisodeCount +
            ", replayBufferSize=" + replayBufferSize +
            ", maximalStepCountBound=" + maximalStepCountBound +
            ", stageCount=" + stageCount +
            ", explorationConstantSupplier=" + explorationConstantSupplier +
            ", temperatureSupplier=" + temperatureSupplier +
            ", riskSupplier=" + riskSupplier +
            ", trainerAlgorithm=" + trainerAlgorithm +
            ", approximatorType=" + approximatorType +
            ", evaluatorType=" + evaluatorType +
            ", selectorType=" + selectorType +
            ", trainingBatchSize=" + trainingBatchSize +
            ", trainingEpochCount=" + trainingEpochCount +
            ", learningRate=" + learningRate +
            ", globalRiskAllowed=" + globalRiskAllowed +
            ", inferenceExistingFlowStrategy=" + inferenceExistingFlowStrategy +
            ", inferenceNonExistingFlowStrategy=" + inferenceNonExistingFlowStrategy +
            ", explorationExistingFlowStrategy=" + explorationExistingFlowStrategy +
            ", explorationNonExistingFlowStrategy=" + explorationNonExistingFlowStrategy +
            ", flowOptimizerType=" + flowOptimizerType +
            ", subTreeRiskCalculatorTypeForKnownFlow=" + subTreeRiskCalculatorTypeForKnownFlow +
            ", subTreeRiskCalculatorTypeForUnknownFlow=" + subTreeRiskCalculatorTypeForUnknownFlow +
            '}';
    }
}
