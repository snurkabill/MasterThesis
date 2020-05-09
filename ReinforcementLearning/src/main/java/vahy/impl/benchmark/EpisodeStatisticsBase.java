package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;

import java.time.Duration;
import java.util.List;

public class EpisodeStatisticsBase implements EpisodeStatistics {

    private final Duration totalDuration;
    private final int playerCount;
    private final List<Double> averagePlayerStepCount;
    private final List<Double> stdevPlayerStepCount;
    private final double averageMillisPerEpisode;
    private final double stdevMillisPerEpisode;
    private final List<List<Double>> totalPayoffAverage;
    private final List<List<Double>> totalPayoffStdev;

    public EpisodeStatisticsBase(Duration totalDuration,
                                 int playerCount,
                                 List<Double> averagePlayerStepCount,
                                 List<Double> stdevPlayerStepCount,
                                 double averageMillisPerEpisode,
                                 double stdevMillisPerEpisode,
                                 List<List<Double>> totalPayoffAverage,
                                 List<List<Double>> totalPayoffStdev) {
        this.totalDuration = totalDuration;
        this.playerCount = playerCount;
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

    public List<Double> getAveragePlayerStepCount() {
        return averagePlayerStepCount;
    }

    public List<Double> getStdevPlayerStepCount() {
        return stdevPlayerStepCount;
    }

    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
    }

    @Override
    public double getStdevMillisPerEpisode() {
        return stdevMillisPerEpisode;
    }

    public List<List<Double>> getTotalPayoffAverage() {
        return totalPayoffAverage;
    }

    public List<List<Double>> getTotalPayoffStdev() {
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
            for (int j = 0; j < totalPayoffAverage.get(i).size(); j++) {
                sb.append(printOneProperty("Total " + j + "th payoff for " + i + "th player", totalPayoffAverage.get(i).get(j), totalPayoffStdev.get(i).get(j)));
            }
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
