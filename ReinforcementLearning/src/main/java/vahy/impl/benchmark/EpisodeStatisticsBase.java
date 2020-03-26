package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;

import java.time.Duration;

public class EpisodeStatisticsBase implements EpisodeStatistics {

    private final Duration totalDuration;
    private final double averagePlayerStepCount;
    private final double stdevPlayerStepCount;
    private final double averageMillisPerEpisode;
    private final double stdevMillisPerEpisode;
    private final double totalPayoffAverage;
    private final double totalPayoffStdev;

    public EpisodeStatisticsBase(Duration totalDuration, double averagePlayerStepCount, double stdevPlayerStepCount, double averageMillisPerEpisode, double stdevMillisPerEpisode, double totalPayoffAverage, double totalPayoffStdev) {
        this.totalDuration = totalDuration;
        this.averagePlayerStepCount = averagePlayerStepCount;
        this.stdevPlayerStepCount = stdevPlayerStepCount;
        this.averageMillisPerEpisode = averageMillisPerEpisode;
        this.stdevMillisPerEpisode = stdevMillisPerEpisode;
        this.totalPayoffAverage = totalPayoffAverage;
        this.totalPayoffStdev = totalPayoffStdev;
    }

    protected String printOneProperty(String name, double average, double stdev) {
        var sb = new StringBuilder();
        sb.append(name);
        sb.append(": average: [");
        sb.append(average);
        sb.append("] stdev: [");
        sb.append(stdev);
        sb.append("].");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public double getAveragePlayerStepCount() {
        return averagePlayerStepCount;
    }

    public double getStdevPlayerStepCount() {
        return stdevPlayerStepCount;
    }

    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
    }

    @Override
    public double getStdevMillisPerEpisode() {
        return stdevMillisPerEpisode;
    }

    public double getTotalPayoffAverage() {
        return totalPayoffAverage;
    }

    public double getTotalPayoffStdev() {
        return totalPayoffStdev;
    }

    @Override
    public String printToLog() {
        var sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("TotalDuration [").append(totalDuration.toMillis()).append("] ms");
        sb.append(System.lineSeparator());
        sb.append(printOneProperty("Player Step Count", averagePlayerStepCount, stdevPlayerStepCount));
        sb.append(printOneProperty("Total Payoff", totalPayoffAverage, totalPayoffStdev));
        sb.append(printOneProperty("Ms per episode", averageMillisPerEpisode, stdevMillisPerEpisode));
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
