package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;

import java.time.Duration;
import java.util.List;

public class EpisodeStatisticsBase implements EpisodeStatistics {

    private final Duration totalDuration;
    private final int playerCount;
    private final List<Double> averagePlayerStepCount;
    private final List<Double> stdevPlayerStepCount;
    private final List<Double> averageDecisionTimeInMillis;
    private final List<Double> stdevDecisionTimeInMillis;
    private final double averageMillisPerEpisode;
    private final double stdevMillisPerEpisode;
    private final List<Double> totalPayoffAverage;
    private final List<Double> totalPayoffStdev;

    public EpisodeStatisticsBase(Duration totalDuration,
                                 int playerCount,
                                 List<Double> averagePlayerStepCount,
                                 List<Double> stdevPlayerStepCount,
                                 List<Double> averageDecisionTimeInMillis,
                                 List<Double> stdevDecisionTimeInMillis,
                                 double averageMillisPerEpisode,
                                 double stdevMillisPerEpisode,
                                 List<Double> totalPayoffAverage,
                                 List<Double> totalPayoffStdev) {
        this.totalDuration = totalDuration;
        this.playerCount = playerCount;
        this.averagePlayerStepCount = averagePlayerStepCount;
        this.stdevPlayerStepCount = stdevPlayerStepCount;
        this.averageDecisionTimeInMillis = averageDecisionTimeInMillis;
        this.stdevDecisionTimeInMillis = stdevDecisionTimeInMillis;
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

    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public Duration getTotalDuration() {
        return totalDuration;
    }

    @Override
    public List<Double> getAveragePlayerStepCount() {
        return averagePlayerStepCount;
    }

    @Override
    public List<Double> getStdevPlayerStepCount() {
        return stdevPlayerStepCount;
    }

    @Override
    public List<Double> getAverageDecisionTimeInMillis() {
        return averageDecisionTimeInMillis;
    }

    @Override
    public List<Double> getStdevDecisionTimeInMillis() {
        return stdevDecisionTimeInMillis;
    }

    @Override
    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
    }

    @Override
    public double getStdevMillisPerEpisode() {
        return stdevMillisPerEpisode;
    }

    @Override
    public List<Double> getTotalPayoffAverage() {
        return totalPayoffAverage;
    }

    @Override
    public List<Double> getTotalPayoffStdev() {
        return totalPayoffStdev;
    }

    @Override
    public String printToLog() {
        var sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("TotalDuration [").append(totalDuration.toMillis()).append("] ms");
        sb.append(System.lineSeparator());
        for (int i = 0; i < playerCount; i++) {
            sb.append(printOneProperty("Player " + i + " StepCount", averagePlayerStepCount.get(i), stdevPlayerStepCount.get(i)));
            sb.append(printOneProperty("Total payoff for " + i + "th player", totalPayoffAverage.get(i), totalPayoffStdev.get(i)));
            sb.append(printOneProperty("Avg decision time " + i + "th player", averageDecisionTimeInMillis.get(i), stdevDecisionTimeInMillis.get(i)));

        }
        sb.append(printOneProperty("Ms per episode", averageMillisPerEpisode, stdevMillisPerEpisode));
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
