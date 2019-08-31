package vahy.impl.search.MCTS;

import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;

public class MonteCarloTreeSearchMetadata extends BaseSearchNodeMetadata {

    private int visitCounter;
    private double sumOfTotalEstimations;

    public MonteCarloTreeSearchMetadata(double cumulativeReward, double gainedReward, double predictedReward) {
        super(cumulativeReward, gainedReward, predictedReward);
        visitCounter = 0;
    }

    public int getVisitCounter() {
        return visitCounter;
    }

    public void increaseVisitCounter() {
        visitCounter++;
    }

    public double getSumOfTotalEstimations() {
        return sumOfTotalEstimations;
    }

    public void setSumOfTotalEstimations(double sumOfTotalEstimations) {
        this.sumOfTotalEstimations = sumOfTotalEstimations;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append("\\nVisitCounter: ");
        stringBuilder.append(this.visitCounter);
//        stringBuilder.append("\\nsumOfTotalE: ");
//        stringBuilder.append(sumOfTotalEstimations != null ? this.sumOfTotalEstimations.toPrettyString() : null);
        return stringBuilder.toString();
    }
}
