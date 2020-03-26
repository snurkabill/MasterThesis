package vahy.api.experiment;

import vahy.utils.EnumUtils;

import java.nio.file.Path;
import java.util.SplittableRandom;

public class SystemConfigBuilder {

    // STOCHASTICITY
    private long randomSeed;
    private StochasticStrategy stochasticStrategy = StochasticStrategy.RANDOM;

    // THREADING
    private boolean singleThreadedEvaluation = false;
    private int parallelThreadsCount = Runtime.getRuntime().availableProcessors() - 1;

    // VISUALIZATION
    private boolean drawWindow = false;

    // Evaluation
    private int evalEpisodeCount;

    private boolean dumpTrainingData = false;
    private boolean dumpEvaluationData = false;
    private Path dumpPath = null;

    private String pythonVirtualEnvPath;


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
    public SystemConfigBuilder setDumpPath(Path dumpPath) {
        this.dumpPath = dumpPath;
        return this;
    }

    public SystemConfigBuilder setPythonVirtualEnvPath(String pythonVirtualEnvPath) {
        this.pythonVirtualEnvPath = pythonVirtualEnvPath;
        return this;
    }

    public SystemConfig buildSystemConfig() {
        return new SystemConfig(resolveRandomSeed(), singleThreadedEvaluation, parallelThreadsCount, drawWindow, evalEpisodeCount, dumpTrainingData, dumpEvaluationData, dumpPath, pythonVirtualEnvPath);
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
