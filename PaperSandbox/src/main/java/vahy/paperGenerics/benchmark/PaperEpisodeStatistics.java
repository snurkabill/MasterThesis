package vahy.paperGenerics.benchmark;

import vahy.impl.benchmark.EpisodeStatisticsBase;

import java.time.Duration;
import java.util.List;

public class PaperEpisodeStatistics extends EpisodeStatisticsBase {

    private final List<Long> riskHitCounter;
    private final List<Double> riskHitRatio;
    private final List<Double> riskHitStdev;

    public PaperEpisodeStatistics(Duration totalDuration,
                                  int playerCount,
                                  List<Double> averagePlayerStepCount,
                                  List<Double> stdevPlayerStepCount,
                                  double averageMillisPerEpisode,
                                  double stdevMillisPerEpisode,
                                  List<Double> totalPayoffAverage,
                                  List<Double> totalPayoffStdev,
                                  List<Long> riskHitCounter,
                                  List<Double> riskHitRatio,
                                  List<Double> riskHitStdev) {
        super(totalDuration, playerCount, averagePlayerStepCount, stdevPlayerStepCount, averageMillisPerEpisode, stdevMillisPerEpisode, totalPayoffAverage, totalPayoffStdev);
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
        this.riskHitStdev = riskHitStdev;
    }

    public List<Long> getRiskHitCounter() {
        return riskHitCounter;
    }

    public List<Double> getRiskHitRatio() {
        return riskHitRatio;
    }

    public List<Double> getRiskHitStdev() {
        return riskHitStdev;
    }

    @Override
    public String printToLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.printToLog());
        sb.append(System.lineSeparator());

        sb.append("Empirical risk average: [");
        for (int i = 0; i < getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] StepCount:").append(riskHitRatio.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk episodes count: [");
        for (int i = 0; i < getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskHitCounter:").append(riskHitCounter.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Empirical risk stdev: [");
        for (int i = 0; i < getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskHitStdev:").append(riskHitStdev.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
