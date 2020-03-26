package vahy.paperGenerics.benchmark;

import vahy.impl.benchmark.EpisodeStatisticsBase;

import java.time.Duration;

public class PaperEpisodeStatistics extends EpisodeStatisticsBase {

    private final long riskHitCounter;
    private final double riskHitRatio;
    private final double riskHitStdev;

    public PaperEpisodeStatistics(Duration totalDuration,
                                  double averagePlayerStepCount,
                                  double stdevPlayerStepCount,
                                  double averageMillisPerEpisode,
                                  double stdevMillisPerEpisode,
                                  double totalPayoffAverage,
                                  double totalPayoffStdev,
                                  long riskHitCounter,
                                  double riskHitRatio,
                                  double riskHitStdev) {
        super(totalDuration, averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, stdevMillisPerEpisode, totalPayoffAverage, totalPayoffStdev);
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
        this.riskHitStdev = riskHitStdev;
    }

    public long getRiskHitCounter() {
        return riskHitCounter;
    }

    public double getRiskHitRatio() {
        return riskHitRatio;
    }

    public double getRiskHitStdev() {
        return riskHitStdev;
    }

    @Override
    public String printToLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.printToLog());
        sb.append(System.lineSeparator());

        sb.append("Empirical risk average: [");
        sb.append(riskHitRatio);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk episodes count: [");
        sb.append(riskHitCounter);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Empirical risk stdev: [");
        sb.append(riskHitStdev);
        sb.append("]");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
