package vahy.api.experiment;

import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;

public class ApproximatorConfigBuilder {

    // TENSORFLOW
    private String creatingScript;

    // NN
    private int trainingBatchSize;
    private int trainingEpochCount;
    private ApproximatorType approximatorType;
    private DataAggregationAlgorithm dataAggregationAlgorithm;

    private int replayBufferSize;

    private double learningRate;

    public ApproximatorConfigBuilder setCreatingScriptName(String creatingScript) {
        this.creatingScript = creatingScript;
        return this;
    }

    public ApproximatorConfigBuilder setTrainingBatchSize(int trainingBatchSize) {

        this.trainingBatchSize = trainingBatchSize;
        return this;
    }

    public ApproximatorConfigBuilder setTrainingEpochCount(int trainingEpochCount) {
        this.trainingEpochCount = trainingEpochCount;
        return this;
    }

    public ApproximatorConfigBuilder setApproximatorType(ApproximatorType approximatorType) {
        this.approximatorType = approximatorType;
        return this;
    }

    public ApproximatorConfigBuilder setDataAggregationAlgorithm(DataAggregationAlgorithm dataAggregationAlgorithm) {
        this.dataAggregationAlgorithm = dataAggregationAlgorithm;
        return this;
    }

    public ApproximatorConfigBuilder setReplayBufferSize(int replayBufferSize) {
        this.replayBufferSize = replayBufferSize;
        return this;
    }

    public ApproximatorConfigBuilder setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public ApproximatorConfig build() {
        if(approximatorType == null) {
            throw new IllegalArgumentException("Approximator algorithm setup is missing. ");
        }
        if(approximatorType == ApproximatorType.DL4J_NN) {
            throw new IllegalArgumentException("Predictor: [" + approximatorType + "] is not supported for now. ");
        }

        if(approximatorType == ApproximatorType.TF_NN) {
            if(creatingScript == null) {
                throw new IllegalArgumentException("Missing TF model creating script.");
            }
            if(trainingBatchSize <= 0) {
                throw new IllegalArgumentException("TrainingBatchSize must be positive. Value: [" + trainingBatchSize + "]");
            }
            if(trainingEpochCount <= 0) {
                throw new IllegalArgumentException("TrainingEpochCount must be positive. Value: [" + trainingEpochCount + "]");
            }
        }
        if(approximatorType == ApproximatorType.TF_NN ||  approximatorType == ApproximatorType.DL4J_NN || approximatorType == ApproximatorType.HASHMAP_LR) {
            if(learningRate <= 0.0) {
                throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
            }
        }
        if(approximatorType == ApproximatorType.HASHMAP || approximatorType == ApproximatorType.HASHMAP_LR) {
            if(replayBufferSize != 0) {
                throw new IllegalArgumentException("Setting batch size with hashmap predictor has no effect. Check Approximator Config.");
            }
        }
        if(dataAggregationAlgorithm == DataAggregationAlgorithm.REPLAY_BUFFER) {
            if(replayBufferSize <= 0) {
                throw new IllegalArgumentException("Replay buffer must have positive size. Value: [" + replayBufferSize + "]");
            }
        }
        return new ApproximatorConfig(
            creatingScript,
            trainingBatchSize,
            trainingEpochCount,
            approximatorType,
            dataAggregationAlgorithm,
            replayBufferSize,
            learningRate);
    }
}
