package vahy.config;

public class SystemConfig {

    // STOCHASTICITY
    private final long randomSeed;

    // THREADING
    private final boolean singleThreadedEvaluation;
    private final int parallelThreadsCount;

    // VISUALIZATION
    private final boolean drawWindow;

    // Evaluation
    private final int evalEpisodeCount;


    public SystemConfig(long randomSeed, boolean singleThreadedEvaluation, int parallelThreadsCount, boolean drawWindow, int evalEpisodeCount) {
        this.randomSeed = randomSeed;
        this.singleThreadedEvaluation = singleThreadedEvaluation;
        this.parallelThreadsCount = parallelThreadsCount;
        this.drawWindow = drawWindow;
        this.evalEpisodeCount = evalEpisodeCount;
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

//    @Override
//    public String toString() {
//        return "SystemConfig{" +
//            "randomSeed=" + randomSeed +
//            ", singleThreadedEvaluation=" + singleThreadedEvaluation +
//            ", parallelThreadsCount=" + parallelThreadsCount +
//            ", drawWindow=" + drawWindow +
//            ", evalEpisodeCount=" + evalEpisodeCount +
//
//            '}';
//    }

    @Override
    public String toString() {
        return "randomSeed," + randomSeed + System.lineSeparator() +
            "singleThreadedEvaluation," + singleThreadedEvaluation + System.lineSeparator() +
            "parallelThreadsCount," + parallelThreadsCount + System.lineSeparator() +
            "drawWindow," + drawWindow + System.lineSeparator() +
            "evalEpisodeCount," + evalEpisodeCount + System.lineSeparator();
    }
}
