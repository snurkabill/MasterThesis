package vahy.impl.search.node.nodeMetadata;

import vahy.api.search.node.NodeMetadata;

import java.util.Arrays;
public class BaseNodeMetadata implements NodeMetadata {

    private final double[] cumulativeReward;
    private final double[] gainedReward;
    private boolean isEvaluated;

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
    public boolean isEvaluated() {
        return this.isEvaluated;
    }

    @Override
    public void setEvaluated() {
        this.isEvaluated = true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.lineSeparator()).append("CumulativeRew: ");
        stringBuilder.append(Arrays.toString(this.cumulativeReward));
        stringBuilder.append(System.lineSeparator()).append("GainedReward: ");
        stringBuilder.append(Arrays.toString(this.gainedReward));

//        stringBuilder.append(System.lineSeparator()).append("PriorProbability: ");
//        stringBuilder.append(this.priorProbability);
//        stringBuilder.append(System.lineSeparator()).append("ChildProbabilities: ");
//        stringBuilder.append(System.lineSeparator()).append(childProbabilities.toString());

        return stringBuilder.toString();
    }
}
