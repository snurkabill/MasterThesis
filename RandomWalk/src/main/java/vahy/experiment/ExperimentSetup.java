package vahy.experiment;

import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;

import java.util.function.Supplier;

public class ExperimentSetup {

    private final long randomSeed;

    // MCTS
    private final double cpuctParameter;

    private final double mcRolloutCount;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;


    // REINFORCEMENT
    private final double discountFactor;
    private final int batchEpisodeCount;
    private final int replayBufferSize;
    private final int maximalStepCountBound;
    private final int stageCount;

    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;

    private final TrainerAlgorithm trainerAlgorithm;
    private final ApproximatorType approximatorType;

    // NN
    private final int trainingBatchSize;
    private final int trainingEpochCount;

    // Evaluation
    private final int evalEpisodeCount;

    // PAPER
    private final double globalRiskAllowed;

    public ExperimentSetup(long randomSeed, double cpuctParameter, double mcRolloutCount, TreeUpdateConditionFactory treeUpdateConditionFactory, double discountFactor, int batchEpisodeCount, int replayBufferSize, int maximalStepCountBound, int stageCount, Supplier<Double> explorationConstantSupplier, Supplier<Double> temperatureSupplier, TrainerAlgorithm trainerAlgorithm, ApproximatorType approximatorType, int trainingBatchSize, int trainingEpochCount, int evalEpisodeCount, double globalRiskAllowed) {
        this.randomSeed = randomSeed;
        this.cpuctParameter = cpuctParameter;
        this.mcRolloutCount = mcRolloutCount;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.discountFactor = discountFactor;
        this.batchEpisodeCount = batchEpisodeCount;
        this.replayBufferSize = replayBufferSize;
        this.maximalStepCountBound = maximalStepCountBound;
        this.stageCount = stageCount;
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.trainerAlgorithm = trainerAlgorithm;
        this.approximatorType = approximatorType;
        this.trainingBatchSize = trainingBatchSize;
        this.trainingEpochCount = trainingEpochCount;
        this.evalEpisodeCount = evalEpisodeCount;
        this.globalRiskAllowed = globalRiskAllowed;
    }

    public double getCpuctParameter() {
        return cpuctParameter;
    }

    public double getMcRolloutCount() {
        return mcRolloutCount;
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

    public TrainerAlgorithm getTrainerAlgorithm() {
        return trainerAlgorithm;
    }

    public ApproximatorType getApproximatorType() {
        return approximatorType;
    }

    public int getTrainingBatchSize() {
        return trainingBatchSize;
    }

    public int getTrainingEpochCount() {
        return trainingEpochCount;
    }

    public int getEvalEpisodeCount() {
        return evalEpisodeCount;
    }

    public double getGlobalRiskAllowed() {
        return globalRiskAllowed;
    }

    public TreeUpdateConditionFactory getTreeUpdateConditionFactory() {
        return treeUpdateConditionFactory;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

}
