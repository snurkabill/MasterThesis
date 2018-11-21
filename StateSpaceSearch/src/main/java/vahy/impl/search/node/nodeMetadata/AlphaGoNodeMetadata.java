package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;

public class AlphaGoNodeMetadata<TReward extends Reward> extends MCTSNodeMetadata<TReward> {

    private final double priorProbability; /// P value

    private TReward totalActionValue; // W value

    public AlphaGoNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward defaultEstimatedTotalReward, double priorProbability) {
        super(cumulativeReward, gainedReward, defaultEstimatedTotalReward);
        this.priorProbability = priorProbability;
        this.totalActionValue = defaultEstimatedTotalReward;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public TReward getTotalActionValue() {
        return totalActionValue;
    }

    public void setTotalActionValue(TReward totalActionValue) {
        this.totalActionValue = totalActionValue;
    }
}
