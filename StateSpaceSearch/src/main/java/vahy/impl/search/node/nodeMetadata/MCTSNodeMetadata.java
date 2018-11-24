package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;

public class MCTSNodeMetadata<TReward extends Reward> extends BaseSearchNodeMetadata<TReward> {

    private int visitCounter;
    private TReward sumOfTotalEstimations;

    public MCTSNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward predictedReward) {
        super(cumulativeReward, gainedReward, predictedReward);
        visitCounter = 0;
    }

    public int getVisitCounter() {
        return visitCounter;
    }

    public void increaseVisitCounter() {
        visitCounter++;
    }

    public TReward getSumOfTotalEstimations() {
        return sumOfTotalEstimations;
    }

    public void setSumOfTotalEstimations(TReward sumOfTotalEstimations) {
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
