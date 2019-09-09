package vahy.paperGenerics.experiment;

import vahy.impl.search.node.SearchNodeImpl;

public class CalculatedResultStatistics {

    private final double averagePlayerStepCount;
    private final double stdevPlayerStepCount;
    private final double averageMillisPerEpisode;
    private final double totalPayoffAverage;
    private final double totalPayoffStdev;
    private final long riskHitCounter;
    private final double riskHitRatio;

    public CalculatedResultStatistics(double averagePlayerStepCount,
                                      double stdevPlayerStepCount,
                                      double averageMillisPerEpisode,
                                      double totalPayoffAverage,
                                      double totalPayoffStdev,
                                      long riskHitCounter,
                                      double riskHitRatio) {
        this.averagePlayerStepCount = averagePlayerStepCount;
        this.stdevPlayerStepCount = stdevPlayerStepCount;
        this.averageMillisPerEpisode = averageMillisPerEpisode;
        this.totalPayoffAverage = totalPayoffAverage;
        this.totalPayoffStdev = totalPayoffStdev;
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
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

    public long getRiskHitCounter() {
        return riskHitCounter;
    }

    public double getRiskHitRatio() {
        return riskHitRatio;
    }

    private String printOneProperty(String name, double average, double stdev) {
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

    public String printToLog() {
        var sb = new StringBuilder();

        sb.append(System.lineSeparator());
        sb.append(printOneProperty("Player Step Count", averagePlayerStepCount, stdevPlayerStepCount));
        sb.append(printOneProperty("Total Payoff", totalPayoffAverage, totalPayoffStdev));

        sb.append("Empirical risk average: [");
        sb.append(riskHitRatio);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Risk episodes count: [");
        sb.append(riskHitCounter);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Milliseconds per episode average: [");
        sb.append(averageMillisPerEpisode);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("TOTAL EXPANDED NODES - BROKEN (TODO: is valid only if algorithm is executed separately ): [");
        sb.append(SearchNodeImpl.nodeInstanceId);
        sb.append("]");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String printToFile() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
