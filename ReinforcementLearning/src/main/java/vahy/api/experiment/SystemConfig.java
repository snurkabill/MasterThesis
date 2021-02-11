package vahy.api.experiment;

import java.nio.file.Path;

public class SystemConfig implements Config {

    // STOCHASTICITY
    private final long randomSeed;

    // THREADING
    private final boolean singleThreadedEvaluation;
    private final int parallelThreadsCount;

    // VISUALIZATION
    private final boolean drawWindow;

    // Evaluation
    private final int evalEpisodeCount;
    private final int evalEpisodeCountDuringTraining;
    private final boolean evaluateDuringTraining;

    private final boolean dumpTrainingData;
    private final boolean dumpEvaluationData;
    private final Path dumpPath;

    public SystemConfig(long randomSeed, boolean singleThreadedEvaluation, int parallelThreadsCount, boolean drawWindow, int evalEpisodeCount, int evalEpisodeCountDuringTraining, boolean evaluateDuringTraining, boolean dumpTrainingData, boolean dumpEvaluationData, Path dumpPath) {
        this.randomSeed = randomSeed;
        this.singleThreadedEvaluation = singleThreadedEvaluation;
        this.parallelThreadsCount = parallelThreadsCount;
        this.drawWindow = drawWindow;
        this.evalEpisodeCount = evalEpisodeCount;
        this.evalEpisodeCountDuringTraining = evalEpisodeCountDuringTraining;
        this.evaluateDuringTraining = evaluateDuringTraining;
        this.dumpTrainingData = dumpTrainingData;
        this.dumpEvaluationData = dumpEvaluationData;
        this.dumpPath = dumpPath;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public boolean isSingleThreadedEvaluation() {
        return singleThreadedEvaluation;
    }

    public int getParallelThreadsCount() {
        return parallelThreadsCount;
    }

    public boolean isDrawWindow() {
        return drawWindow;
    }

    public int getEvalEpisodeCount() {
        return evalEpisodeCount;
    }

    public int getEvalEpisodeCountDuringTraining() {
        return evalEpisodeCountDuringTraining;
    }

    public boolean isEvaluateDuringTraining() {
        return evaluateDuringTraining;
    }

    public boolean dumpTrainingData() {
        return dumpTrainingData;
    }

    public boolean dumpEvaluationData() {
        return dumpEvaluationData;
    }

    public Path getDumpPath() {
        return dumpPath;
    }

    @Override
    public String toString() {
        return "randomSeed," + randomSeed + System.lineSeparator() +
            "singleThreadedEvaluation," + singleThreadedEvaluation + System.lineSeparator() +
            "parallelThreadsCount," + parallelThreadsCount + System.lineSeparator() +
            "drawWindow," + drawWindow + System.lineSeparator() +
            "evalEpisodeCount," + evalEpisodeCount + System.lineSeparator() +
            "evalDuringTraining," + evaluateDuringTraining + System.lineSeparator() +
            "evalEpisodeCountDuringTraining," + evalEpisodeCountDuringTraining + System.lineSeparator() +
            "dumpTrainingData," + dumpTrainingData + System.lineSeparator() +
            "dumpEvaluationData," + dumpEvaluationData + System.lineSeparator() +
            "dumpPath," + dumpPath + System.lineSeparator();
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
