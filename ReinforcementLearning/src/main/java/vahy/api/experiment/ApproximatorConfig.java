package vahy.api.experiment;

import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;

public class ApproximatorConfig implements Config {

    // TENSORFLOW
    private final String creatingScript;

    // NN
    private final int trainingBatchSize;
    private final int trainingEpochCount;
    private final ApproximatorType approximatorType;
    private final DataAggregationAlgorithm dataAggregationAlgorithm;

    private final int replayBufferSize;

    private final double learningRate;
    private final double dropoutKeepProbability;

    public ApproximatorConfig(String creatingScript,
                              int trainingBatchSize,
                              int trainingEpochCount,
                              ApproximatorType approximatorType,
                              DataAggregationAlgorithm dataAggregationAlgorithm,
                              int replayBufferSize,
                              double learningRate,
                              double dropoutKeepProbability)
    {
        this.creatingScript = creatingScript;
        this.trainingBatchSize = trainingBatchSize;
        this.trainingEpochCount = trainingEpochCount;
        this.approximatorType = approximatorType;

        this.dataAggregationAlgorithm = dataAggregationAlgorithm;
        this.replayBufferSize = replayBufferSize;
        this.learningRate = learningRate;
        this.dropoutKeepProbability = dropoutKeepProbability;
    }

    public String getCreatingScript() {
        return creatingScript;
    }

    public int getTrainingBatchSize() {
        return trainingBatchSize;
    }

    public int getTrainingEpochCount() {
        return trainingEpochCount;
    }

    public ApproximatorType getApproximatorType() {
        return approximatorType;
    }

    public DataAggregationAlgorithm getDataAggregationAlgorithm() {
        return dataAggregationAlgorithm;
    }

    public int getReplayBufferSize() {
        return replayBufferSize;
    }

    public double getLearningRate() {
        return learningRate;
    }

    @Override
    public String toLog() {
        return "TODO: implement this";
    }

    @Override
    public String toFile() {
        return "TODO: implement this";
    }

    public double getDropoutKeepProbability() {
        return dropoutKeepProbability;
    }
}
