package vahy.paperGenerics.benchmark;

import vahy.impl.benchmark.EpisodeStatisticsBase;

public class PaperEpisodeStatistics extends EpisodeStatisticsBase {

    private final long riskHitCounter;
    private final double riskHitRatio;

    public PaperEpisodeStatistics(double averagePlayerStepCount,
                                  double stdevPlayerStepCount,
                                  double averageMillisPerEpisode,
                                  double totalPayoffAverage,
                                  double totalPayoffStdev,
                                  long riskHitCounter,
                                  double riskHitRatio) {
        super(averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, totalPayoffAverage, totalPayoffStdev);
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
    }

    public long getRiskHitCounter() {
        return riskHitCounter;
    }

    public double getRiskHitRatio() {
        return riskHitRatio;
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

        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
