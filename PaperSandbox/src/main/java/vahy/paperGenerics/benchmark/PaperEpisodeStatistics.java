package vahy.paperGenerics.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.impl.benchmark.EpisodeStatisticsBase;

import java.time.Duration;
import java.util.List;

public class PaperEpisodeStatistics implements EpisodeStatistics {

    private final EpisodeStatisticsBase base;
    private final List<Long> riskHitCounter;
    private final List<Double> riskHitRatio;
    private final List<Double> riskHitStdev;
    private final List<Double> riskExhaustedIndexAverage;
    private final List<Double> riskExhaustedIndexStdev;
    private final List<Double> riskThresholdAtEndAverage;
    private final List<Double> riskThresholdAtEndStdev;


    public PaperEpisodeStatistics(EpisodeStatisticsBase base, List<Long> riskHitCounter, List<Double> riskHitRatio, List<Double> riskHitStdev, List<Double> riskExhaustedIndexAverage, List<Double> riskExhaustedIndexStdev, List<Double> riskThresholdAtEndAverage, List<Double> riskThresholdAtEndStdev) {
        this.base = base;
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
        this.riskHitStdev = riskHitStdev;
        this.riskExhaustedIndexAverage = riskExhaustedIndexAverage;
        this.riskExhaustedIndexStdev = riskExhaustedIndexStdev;
        this.riskThresholdAtEndAverage = riskThresholdAtEndAverage;
        this.riskThresholdAtEndStdev = riskThresholdAtEndStdev;
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

    public List<Double> getRiskExhaustedIndexAverage() {
        return riskExhaustedIndexAverage;
    }

    public List<Double> getRiskExhaustedIndexStdev() {
        return riskExhaustedIndexStdev;
    }

    public List<Double> getRiskThresholdAtEndAverage() {
        return riskThresholdAtEndAverage;
    }

    public List<Double> getRiskThresholdAtEndStdev() {
        return riskThresholdAtEndStdev;
    }

    @Override
    public Duration getTotalDuration() {
        return base.getTotalDuration();
    }

    @Override
    public List<Double> getAveragePlayerStepCount() {
        return base.getAveragePlayerStepCount();
    }

    @Override
    public List<Double> getStdevPlayerStepCount() {
        return base.getStdevPlayerStepCount();
    }

    @Override
    public List<Double> getAverageDecisionTimeInMillis() {
        return base.getAverageDecisionTimeInMillis();
    }

    @Override
    public List<Double> getStdevDecisionTimeInMillis() {
        return base.getStdevDecisionTimeInMillis();
    }

    @Override
    public double getAverageMillisPerEpisode() {
        return base.getAverageMillisPerEpisode();
    }

    @Override
    public double getStdevMillisPerEpisode() {
        return base.getStdevMillisPerEpisode();
    }

    @Override
    public List<Double> getTotalPayoffAverage() {
        return base.getTotalPayoffAverage();
    }

    @Override
    public List<Double> getTotalPayoffStdev() {
        return base.getTotalPayoffStdev();
    }

    @Override
    public String printToLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(base.printToLog());
        sb.append(System.lineSeparator());

        sb.append("Empirical risk average: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] StepCount:").append(riskHitRatio.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk episodes count: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskHitCounter:").append(riskHitCounter.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Empirical risk stdev: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskHitStdev:").append(riskHitStdev.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk Threshold at the end average: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskThresholdAtTheEndAverage:").append(riskThresholdAtEndAverage.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk Threshold at the end stdev: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskThresholdAtTheEndStdev:").append(riskThresholdAtEndStdev.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk exhausted index average: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskExhaustedIndexAverage:").append(riskExhaustedIndexAverage.get(i));
        }
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk exhausted index stdev: [");
        for (int i = 0; i < base.getPlayerCount(); i++) {
            sb.append("Player [").append(i).append("] RiskExhaustedIndexStdev:").append(riskExhaustedIndexStdev.get(i));
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
