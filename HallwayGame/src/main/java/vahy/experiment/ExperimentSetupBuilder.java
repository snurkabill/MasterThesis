package vahy.experiment;

import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.data.HallwayInstance;
import vahy.paperGenerics.experiment.EvaluatorType;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.riskBasedSearch.SelectorType;

import java.util.function.Supplier;

public class ExperimentSetupBuilder {

    private long randomSeed;
    private HallwayInstance hallwayInstance;

    // MCTS
    private double cpuctParameter;
    private TreeUpdateConditionFactory treeUpdateConditionFactory;
    private double mcRolloutCount;


    // REINFORCEMENT
    private double discountFactor;
    private int batchEpisodeCount;
    private int replayBufferSize;
    private int maximalStepCountBound;
    private int stageCount;

    private Supplier<Double> explorationConstantSupplier;
    private Supplier<Double> temperatureSupplier;
    private Supplier<Double> riskSupplier;

    private TrainerAlgorithm trainerAlgorithm;
    private ApproximatorType approximatorType;
    private EvaluatorType evaluatorType;

    // NN
    private int trainingBatchSize;
    private int trainingEpochCount;

    private double learningRate;

    // Evaluation
    private int evalEpisodeCount;

    // PAPER
    private double globalRiskAllowed;
    private SelectorType selectorType;

    private InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private FlowOptimizerType flowOptimizerType;
    private SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow;
    private SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow;

    private boolean omitProbabilities;

    public ExperimentSetupBuilder() {
    }

    public ExperimentSetupBuilder randomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public ExperimentSetupBuilder hallwayInstance(HallwayInstance hallwayInstance) {
        this.hallwayInstance = hallwayInstance;
        return this;
    }

    public ExperimentSetupBuilder cpuctParameter(double cpuctParameter) {
        this.cpuctParameter = cpuctParameter;
        return this;
    }

    public ExperimentSetupBuilder mcRolloutCount(double mcRolloutCount) {
        this.mcRolloutCount = mcRolloutCount;
        return this;
    }

    public ExperimentSetupBuilder discountFactor(double discountFactor) {
        this.discountFactor = discountFactor; return this;
    }

    public ExperimentSetupBuilder batchEpisodeCount(int batchEpisodeCount) {
        this.batchEpisodeCount = batchEpisodeCount; return this;
    }

    public ExperimentSetupBuilder replayBufferSize(int replayBufferSize) {
        this.replayBufferSize = replayBufferSize; return this;
    }

    public ExperimentSetupBuilder maximalStepCountBound(int maximalStepCountBound) {
        this.maximalStepCountBound = maximalStepCountBound; return this;
    }

    public ExperimentSetupBuilder stageCount(int stageCount) {
        this.stageCount = stageCount; return this;
    }

    public ExperimentSetupBuilder explorationConstantSupplier(Supplier<Double> explorationConstantSupplier) {
        this.explorationConstantSupplier = explorationConstantSupplier; return this;
    }

    public ExperimentSetupBuilder temperatureSupplier(Supplier<Double> temperatureSupplier) {
        this.temperatureSupplier = temperatureSupplier; return this;
    }

    public ExperimentSetupBuilder riskSupplier(Supplier<Double> riskSupplier) {
        this.riskSupplier = riskSupplier; return this;
    }

    public ExperimentSetupBuilder trainerAlgorithm(TrainerAlgorithm trainerAlgorithm) {
        this.trainerAlgorithm = trainerAlgorithm; return this;
    }

    public ExperimentSetupBuilder approximatorType(ApproximatorType approximatorType) {
        this.approximatorType = approximatorType; return this;
    }

    public ExperimentSetupBuilder evaluatorType(EvaluatorType evaluatorType) {
        this.evaluatorType = evaluatorType; return this;
    }

    public ExperimentSetupBuilder trainingBatchSize(int trainingBatchSize) {
        this.trainingBatchSize = trainingBatchSize; return this;
    }

    public ExperimentSetupBuilder trainingEpochCount(int trainingEpochCount) {
        this.trainingEpochCount = trainingEpochCount; return this;
    }

    public ExperimentSetupBuilder evalEpisodeCount(int evalEpisodeCount) {
        this.evalEpisodeCount = evalEpisodeCount; return this;
    }

    public ExperimentSetupBuilder learningRate(double learningRate) {
        this.learningRate = learningRate; return this;
    }

    public ExperimentSetupBuilder globalRiskAllowed(double globalRiskAllowed) {
        this.globalRiskAllowed = globalRiskAllowed; return this;
    }

    public ExperimentSetupBuilder treeUpdateConditionFactory(TreeUpdateConditionFactory treeUpdateConditionFactory) {
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        return this;
    }

    public ExperimentSetupBuilder selectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
        return this;
    }

    public ExperimentSetupBuilder omitProbabilities(boolean omitProbabilities) {
        this.omitProbabilities = omitProbabilities;
        return this;
    }

    public ExperimentSetupBuilder setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy inferenceExistingFlowStrategy) {
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        return this;
    }

    public ExperimentSetupBuilder setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy) {
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        return this;
    }

    public ExperimentSetupBuilder setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy explorationExistingFlowStrategy) {
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        return this;
    }

    public ExperimentSetupBuilder setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy) {
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        return this;
    }

    public ExperimentSetupBuilder setFlowOptimizerType(FlowOptimizerType flowOptimizerType) {
        this.flowOptimizerType = flowOptimizerType;
        return this;
    }

    public ExperimentSetupBuilder setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow) {
        this.subTreeRiskCalculatorTypeForKnownFlow = subTreeRiskCalculatorTypeForKnownFlow;
        return this;
    }

    public ExperimentSetupBuilder setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow) {
        this.subTreeRiskCalculatorTypeForUnknownFlow = subTreeRiskCalculatorTypeForUnknownFlow;
        return this;
    }

    public ExperimentSetup buildExperimentSetup() {
        return new ExperimentSetup(
            randomSeed,
            hallwayInstance,
            cpuctParameter,
            mcRolloutCount,
            treeUpdateConditionFactory,
            discountFactor,
            batchEpisodeCount,
            replayBufferSize,
            maximalStepCountBound,
            stageCount,
            explorationConstantSupplier,
            temperatureSupplier,
            riskSupplier,
            trainerAlgorithm,
            approximatorType,
            evaluatorType,
            trainingBatchSize,
            trainingEpochCount,
            learningRate,
            evalEpisodeCount,
            globalRiskAllowed,
            selectorType,
            inferenceExistingFlowStrategy,
            inferenceNonExistingFlowStrategy,
            explorationExistingFlowStrategy,
            explorationNonExistingFlowStrategy,
            flowOptimizerType,
            subTreeRiskCalculatorTypeForKnownFlow,
            subTreeRiskCalculatorTypeForUnknownFlow,
            omitProbabilities);
    }
}
