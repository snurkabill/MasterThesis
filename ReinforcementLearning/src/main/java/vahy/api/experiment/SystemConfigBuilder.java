package vahy.api.experiment;

import vahy.utils.EnumUtils;

import java.util.SplittableRandom;

public class SystemConfigBuilder {

    // STOCHASTICITY
    private long randomSeed;
    private StochasticStrategy stochasticStrategy = StochasticStrategy.RANDOM;

    // THREADING
    private boolean singleThreadedEvaluation;
    private int parallelThreadsCount;

    // VISUALIZATION
    private boolean drawWindow;

    // Evaluation
    private int evalEpisodeCount;

    private boolean dumpTrainingData;
    private boolean dumpEvaluationData;


    public SystemConfigBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public SystemConfigBuilder setStochasticStrategy(StochasticStrategy stochasticStrategy) {
        this.stochasticStrategy = stochasticStrategy;
        return this;
    }

    public SystemConfigBuilder setSingleThreadedEvaluation(boolean singleThreadedEvaluation) {
        this.singleThreadedEvaluation = singleThreadedEvaluation;
        return this;
    }

    public SystemConfigBuilder setParallelThreadsCount(int parallelThreadsCount) {
        this.parallelThreadsCount = parallelThreadsCount;
        return this;
    }

    public SystemConfigBuilder setDrawWindow(boolean drawWindow) {
        this.drawWindow = drawWindow;
        return this;
    }

    public SystemConfigBuilder setEvalEpisodeCount(int evalEpisodeCount) {
        this.evalEpisodeCount = evalEpisodeCount;
        return this;
    }

    public SystemConfigBuilder setDumpTrainingData(boolean dumpTrainingData) {
        this.dumpTrainingData = dumpTrainingData;
        return this;
    }

    public SystemConfigBuilder setDumpEvaluationData(boolean dumpEvaluationData) {
        this.dumpEvaluationData = dumpEvaluationData;
        return this;
    }

    public SystemConfig buildSystemConfig() {
        return new SystemConfig(resolveRandomSeed(), singleThreadedEvaluation, parallelThreadsCount, drawWindow, evalEpisodeCount, dumpTrainingData, dumpEvaluationData);
    }

    private long resolveRandomSeed() {
        switch(stochasticStrategy) {
            case RANDOM:
                return new SplittableRandom().nextLong();
            case REPRODUCIBLE:
                return randomSeed;
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(stochasticStrategy);
        }
    }

}
