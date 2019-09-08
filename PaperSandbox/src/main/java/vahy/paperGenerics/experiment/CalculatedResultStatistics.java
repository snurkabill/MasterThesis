package vahy.paperGenerics.experiment;

import vahy.impl.search.node.SearchNodeImpl;

public class CalculatedResultStatistics {

    private final double averagePlayerStepCount;
    private final double averageMillisPerEpisode;
    private final double totalPayoffAverage;
    private final double totalPayoffStdev;
    private final long riskHitCounter;
    private final double riskHitRatio;

    public CalculatedResultStatistics(double averagePlayerStepCount, double averageMillisPerEpisode, double totalPayoffAverage, double totalPayoffStdev, long riskHitCounter, double riskHitRatio) {
        this.averagePlayerStepCount = averagePlayerStepCount;
        this.averageMillisPerEpisode = averageMillisPerEpisode;
        this.totalPayoffAverage = totalPayoffAverage;
        this.totalPayoffStdev = totalPayoffStdev;
        this.riskHitCounter = riskHitCounter;
        this.riskHitRatio = riskHitRatio;
    }

    public double getAveragePlayerStepCount() {
        return averagePlayerStepCount;
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

    public String printToLog() {
        var sb = new StringBuilder();

        sb.append("Player step count average: [");
        sb.append(averagePlayerStepCount);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Total Payoff average: [");
        sb.append(totalPayoffAverage);
        sb.append("]");
        sb.append(System.lineSeparator());

        sb.append("Total payoff stdev: [");
        sb.append(totalPayoffStdev);
        sb.append("]");
        sb.append(System.lineSeparator());

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
