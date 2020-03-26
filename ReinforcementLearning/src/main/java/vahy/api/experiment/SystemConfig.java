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


    private final boolean dumpTrainingData;
    private final boolean dumpEvaluationData;
    private final Path dumpPath;

    private final String pythonVirtualEnvPath;

    public SystemConfig(long randomSeed, boolean singleThreadedEvaluation, int parallelThreadsCount, boolean drawWindow, int evalEpisodeCount, boolean dumpTrainingData, boolean dumpEvaluationData, Path dumpPath, String pythonVirtualEnvPath) {
        this.randomSeed = randomSeed;
        this.singleThreadedEvaluation = singleThreadedEvaluation;
        this.parallelThreadsCount = parallelThreadsCount;
        this.drawWindow = drawWindow;
        this.evalEpisodeCount = evalEpisodeCount;
        this.dumpTrainingData = dumpTrainingData;
        this.dumpEvaluationData = dumpEvaluationData;
        this.dumpPath = dumpPath;
        this.pythonVirtualEnvPath = pythonVirtualEnvPath;
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

    public boolean dumpTrainingData() {
        return dumpTrainingData;
    }

    public boolean dumpEvaluationData() {
        return dumpEvaluationData;
    }

    public Path getDumpPath() {
        return dumpPath;
    }

    public String getPythonVirtualEnvPath() {
        return pythonVirtualEnvPath;
    }

        @Override
    public String toString() {
        return "randomSeed," + randomSeed + System.lineSeparator() +
            "singleThreadedEvaluation," + singleThreadedEvaluation + System.lineSeparator() +
            "parallelThreadsCount," + parallelThreadsCount + System.lineSeparator() +
            "drawWindow," + drawWindow + System.lineSeparator() +
            "evalEpisodeCount," + evalEpisodeCount + System.lineSeparator() +
            "dumpTrainingData," + dumpTrainingData + System.lineSeparator() +
            "dumpEvaluationData," + dumpEvaluationData + System.lineSeparator() +
            "dumpPath," + dumpPath + System.lineSeparator() +
            "pythonVirtualEnvPath," + pythonVirtualEnvPath + System.lineSeparator();
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
