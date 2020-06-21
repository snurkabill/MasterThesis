package vahy.impl.search.node.nodeMetadata;

import vahy.api.search.node.NodeMetadata;

import java.util.Arrays;

public class BaseNodeMetadata implements NodeMetadata {

    private final double[] cumulativeReward;
    private final double[] gainedReward;

    public BaseNodeMetadata(double[] cumulativeReward, double[] gainedReward) {
        this.cumulativeReward = cumulativeReward;
        this.gainedReward = gainedReward;
    }

    @Override
    public double[] getCumulativeReward() {
        return cumulativeReward;
    }

    @Override
    public double[] getGainedReward() {
        return this.gainedReward;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\\n").append("CumulativeRew: ");
        stringBuilder.append(Arrays.toString(this.cumulativeReward));
        stringBuilder.append("\\n").append("GainedReward: ");
        stringBuilder.append(Arrays.toString(this.gainedReward));
        return stringBuilder.toString();
    }
}
