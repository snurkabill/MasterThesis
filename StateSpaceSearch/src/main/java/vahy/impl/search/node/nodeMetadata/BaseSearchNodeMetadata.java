package vahy.impl.search.node.nodeMetadata;

import vahy.api.search.node.SearchNodeMetadata;

public class BaseSearchNodeMetadata implements SearchNodeMetadata {

    private final double cumulativeReward;
    private final double gainedReward;
    private double predictedReward;
    private double expectedReward;

    public BaseSearchNodeMetadata(double cumulativeReward, double gainedReward, double predictedReward) {
        this.cumulativeReward = cumulativeReward;
        this.gainedReward = gainedReward;
        this.predictedReward = predictedReward;
        this.expectedReward = predictedReward;
    }

    @Override
    public double getPredictedReward() {
        return predictedReward;
    }

    @Override
    public double getCumulativeReward() {
        return cumulativeReward;
    }

    @Override
    public double getGainedReward() {
        return this.gainedReward;
    }

    @Override
    public double getExpectedReward() {
        return expectedReward;
    }

    @Override
    public void setPredictedReward(double predictedReward) {
        this.predictedReward = predictedReward;
    }

    @Override
    public void setExpectedReward(double expectedReward) {
        this.expectedReward = expectedReward;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\nCumulativeRew: ");
        stringBuilder.append(this.cumulativeReward);
        stringBuilder.append("\\nExpectedRew: ");
        stringBuilder.append(this.expectedReward);
        stringBuilder.append("\\nGainedReward: ");
        stringBuilder.append(this.gainedReward);
        stringBuilder.append("\\nPredictedReward: ");
        stringBuilder.append(this.predictedReward);
        return stringBuilder.toString();
    }
}
