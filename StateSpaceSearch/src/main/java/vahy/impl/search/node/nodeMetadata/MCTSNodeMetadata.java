package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;

public class MCTSNodeMetadata<TReward extends Reward> extends BaseSearchNodeMetadata<TReward> {

    private int visitCounter;
    private TReward sumOfTotalEstimations;

    public MCTSNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward defaultEstimationTotalReward) {
        super(cumulativeReward, gainedReward, defaultEstimationTotalReward);
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
}
