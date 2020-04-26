package vahy.config;

import vahy.api.experiment.AlgorithmConfig;
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

public class PaperAlgorithmConfig implements AlgorithmConfig {

    private final String algorithmId;

    // MCTS
    private final double cpuctParameter;

    private final TreeUpdateConditionFactory treeUpdateConditionFactory;

    // REINFORCEMENT
    private final double discountFactor;
    private final int batchEpisodeCount;

    private final int stageCount;

    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    private final EvaluatorType evaluatorType;
    private final SelectorType selectorType;

    private final ApproximatorConfig playerApproximatorConfig;
    private final ApproximatorConfig opponentApproximatorConfig;

    // PAPER
    private final double globalRiskAllowed;
    private final InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private final InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private final ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private final ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private final FlowOptimizerType flowOptimizerType;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow;

    private final int batchedEvaluationSize;
    private final NoiseStrategy noiseStrategy;

    public PaperAlgorithmConfig(String algorithmId,
                                double cpuctParameter,
                                TreeUpdateConditionFactory treeUpdateConditionFactory,
                                double discountFactor,
                                int batchEpisodeCount,
                                int stageCount,
                                Supplier<Double> explorationConstantSupplier,
                                Supplier<Double> temperatureSupplier,
                                Supplier<Double> riskSupplier,
                                EvaluatorType evaluatorType,
                                SelectorType selectorType,
                                ApproximatorConfig playerApproximatorConfig,
                                ApproximatorConfig opponentApproximatorConfig,
                                double globalRiskAllowed,
                                InferenceExistingFlowStrategy inferenceExistingFlowStrategy,
                                InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy,
                                ExplorationExistingFlowStrategy explorationExistingFlowStrategy,
                                ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy,
                                FlowOptimizerType flowOptimizerType,
                                SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow,
                                SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow,
                                int batchedEvaluationSize,
                                NoiseStrategy noiseStrategy) {
        this.playerApproximatorConfig = playerApproximatorConfig;
        this.opponentApproximatorConfig = opponentApproximatorConfig;
        if(algorithmId == null) {
            throw new IllegalArgumentException("PolicyId is missing.");
        }
        this.algorithmId = algorithmId;
        this.cpuctParameter = cpuctParameter;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.discountFactor = discountFactor;
        this.batchEpisodeCount = batchEpisodeCount;
        this.stageCount = stageCount;
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.riskSupplier = riskSupplier;
        this.evaluatorType = evaluatorType;
        this.selectorType = selectorType;
        this.globalRiskAllowed = globalRiskAllowed;
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        this.flowOptimizerType = flowOptimizerType;
        this.subTreeRiskCalculatorTypeForKnownFlow = subTreeRiskCalculatorTypeForKnownFlow;
        this.subTreeRiskCalculatorTypeForUnknownFlow = subTreeRiskCalculatorTypeForUnknownFlow;
        this.batchedEvaluationSize = batchedEvaluationSize;
        this.noiseStrategy = noiseStrategy;
    }

    public double getCpuctParameter() {
        return cpuctParameter;
    }

    public TreeUpdateConditionFactory getTreeUpdateConditionFactory() {
        return treeUpdateConditionFactory;
    }

    @Override
    public String getAlgorithmId() {
        return algorithmId;
    }

    public double getDiscountFactor() {
        return discountFactor;
    }

    public int getBatchEpisodeCount() {
        return batchEpisodeCount;
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

    public EvaluatorType getEvaluatorType() {
        return evaluatorType;
    }

    public SelectorType getSelectorType() {
        return selectorType;
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

    public int getBatchedEvaluationSize() {
        return batchedEvaluationSize;
    }

    public NoiseStrategy getNoiseStrategy() {
        return noiseStrategy;
    }

    public ApproximatorConfig getPlayerApproximatorConfig() {
        return playerApproximatorConfig;
    }

    public ApproximatorConfig getOpponentApproximatorConfig() {
        return opponentApproximatorConfig;
    }
//    @Override
//    public String toString() {
//        return "AlgorithmConfig{" +
//            "cpuctParameter=" + cpuctParameter +
//            ", treeUpdateConditionFactory=" + treeUpdateConditionFactory +
//            ", discountFactor=" + discountFactor +
//            ", batchEpisodeCount=" + batchEpisodeCount +
//            ", replayBufferSize=" + replayBufferSize +
//            ", maximalStepCountBound=" + maximalStepCountBound +
//            ", stageCount=" + stageCount +
//            ", explorationConstantSupplier=" + explorationConstantSupplier +
//            ", temperatureSupplier=" + temperatureSupplier +
//            ", riskSupplier=" + riskSupplier +
//            ", trainerAlgorithm=" + dataAggregationAlgorithm +
//            ", approximatorType=" + approximatorType +
//            ", evaluatorType=" + evaluatorType +
//            ", selectorType=" + selectorType +
//            ", trainingBatchSize=" + trainingBatchSize +
//            ", trainingEpochCount=" + trainingEpochCount +
//            ", learningRate=" + learningRate +
//            ", globalRiskAllowed=" + globalRiskAllowed +
//            ", inferenceExistingFlowStrategy=" + inferenceExistingFlowStrategy +
//            ", inferenceNonExistingFlowStrategy=" + inferenceNonExistingFlowStrategy +
//            ", explorationExistingFlowStrategy=" + explorationExistingFlowStrategy +
//            ", explorationNonExistingFlowStrategy=" + explorationNonExistingFlowStrategy +
//            ", flowOptimizerType=" + flowOptimizerType +
//            ", subTreeRiskCalculatorTypeForKnownFlow=" + subTreeRiskCalculatorTypeForKnownFlow +
//            ", subTreeRiskCalculatorTypeForUnknownFlow=" + subTreeRiskCalculatorTypeForUnknownFlow +
//            ", batchedEvaluationSize=" + batchedEvaluationSize +
//            '}';
//    }

    @Override
    public String toString() {
        return
            "algorithmId," + algorithmId + System.lineSeparator() +
            "cpuctParameter," + cpuctParameter + System.lineSeparator() +
            "treeUpdateConditionFactory," + treeUpdateConditionFactory + System.lineSeparator() +
            "discountFactor," + discountFactor + System.lineSeparator() +
            "batchEpisodeCount," + batchEpisodeCount + System.lineSeparator() +
            "stageCount," + stageCount + System.lineSeparator() +
            "explorationConstantSupplier," + explorationConstantSupplier + System.lineSeparator() +
            "temperatureSupplier," + temperatureSupplier + System.lineSeparator() +
            "riskSupplier," + riskSupplier + System.lineSeparator() +
            "evaluatorType," + evaluatorType + System.lineSeparator() +
            "selectorType," + selectorType + System.lineSeparator() +
            "globalRiskAllowed," + globalRiskAllowed + System.lineSeparator() +
            "inferenceExistingFlowStrategy," + inferenceExistingFlowStrategy + System.lineSeparator() +
            "inferenceNonExistingFlowStrategy," + inferenceNonExistingFlowStrategy + System.lineSeparator() +
            "explorationExistingFlowStrategy," + explorationExistingFlowStrategy + System.lineSeparator() +
            "explorationNonExistingFlowStrategy," + explorationNonExistingFlowStrategy + System.lineSeparator() +
            "flowOptimizerType," + flowOptimizerType + System.lineSeparator() +
            "subTreeRiskCalculatorTypeForKnownFlow," + subTreeRiskCalculatorTypeForKnownFlow + System.lineSeparator() +
            "subTreeRiskCalculatorTypeForUnknownFlow," + subTreeRiskCalculatorTypeForUnknownFlow + System.lineSeparator() +
            "playerApproximatorConfig," + playerApproximatorConfig.toString() + System.lineSeparator() +
                (opponentApproximatorConfig != null ? "OpponentApproximatorConfig," + opponentApproximatorConfig.toString() + System.lineSeparator() : "known_model")  +
            "batchedEvaluationSize," + batchedEvaluationSize + System.lineSeparator() +
            "noiseStrategy, " + noiseStrategy + System.lineSeparator();
    }

    @Override
    public String toLog() {
        return toString();
    }

    @Override
    public String toFile() {
        return toString();
    }
}
