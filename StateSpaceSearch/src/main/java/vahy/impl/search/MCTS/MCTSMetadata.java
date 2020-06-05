package vahy.impl.search.MCTS;

import vahy.impl.search.node.nodeMetadata.BaseNodeMetadata;

import java.util.Arrays;

public class MCTSMetadata extends BaseNodeMetadata {

    private final double[] predictedReward;
    private final double[] expectedReward;
    private final double[] sumOfTotalEstimations;
    private int visitCounter;

    public MCTSMetadata(double[] cumulativeReward, double[] gainedReward) {
        super(cumulativeReward, gainedReward);
        this.predictedReward = new double[cumulativeReward.length];
        this.expectedReward = new double[cumulativeReward.length];
        this.sumOfTotalEstimations = new double[cumulativeReward.length];
        visitCounter = 0;
    }

    public double[] getPredictedReward() {
        return predictedReward;
    }

    public double[] getExpectedReward() {
        return expectedReward;
    }

    public double[] getSumOfTotalEstimations() {
        return sumOfTotalEstimations;
    }

    public int getVisitCounter() {
        return visitCounter;
    }

    public void increaseVisitCounter() {
        visitCounter++;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append(System.lineSeparator()).append("ExpectedRew: ");
        stringBuilder.append(Arrays.toString(this.expectedReward));
        stringBuilder.append(System.lineSeparator()).append("PredictedReward: ");
        stringBuilder.append(Arrays.toString(this.predictedReward));
        stringBuilder.append(System.lineSeparator()).append("VisitCounter: ");
        stringBuilder.append(this.visitCounter);
//        stringBuilder.append(System.lineSeparator()).append("nsumOfTotalE: ");
//        stringBuilder.append(sumOfTotalEstimations != null ? this.sumOfTotalEstimations.toPrettyString() : null);
        return stringBuilder.toString();
    }
}
