package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;

public class EpisodeStatisticsBase implements EpisodeStatistics {

    private final double averagePlayerStepCount;
    private final double stdevPlayerStepCount;
    private final double averageMillisPerEpisode;
    private final double totalPayoffAverage;
    private final double totalPayoffStdev;

    public EpisodeStatisticsBase(double averagePlayerStepCount, double stdevPlayerStepCount, double averageMillisPerEpisode, double totalPayoffAverage, double totalPayoffStdev) {
        this.averagePlayerStepCount = averagePlayerStepCount;
        this.stdevPlayerStepCount = stdevPlayerStepCount;
        this.averageMillisPerEpisode = averageMillisPerEpisode;
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

    public double getAveragePlayerStepCount() {
        return averagePlayerStepCount;
    }

    public double getStdevPlayerStepCount() {
        return stdevPlayerStepCount;
    }

    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
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
        sb.append(printOneProperty("Player Step Count", averagePlayerStepCount, stdevPlayerStepCount));
        sb.append(printOneProperty("Total Payoff", totalPayoffAverage, totalPayoffStdev));

        sb.append("Milliseconds per episode average: [");
        sb.append(averageMillisPerEpisode);
        sb.append("]");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
