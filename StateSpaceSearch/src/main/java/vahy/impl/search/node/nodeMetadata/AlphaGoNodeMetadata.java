package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.reward.Reward;

public class AlphaGoNodeMetadata<TReward extends Reward> extends MCTSNodeMetadata<TReward> {

    private final double priorProbability; /// P value

    private TReward totalActionValue; // W value

    public AlphaGoNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward predictedReward, double priorProbability) {
        super(cumulativeReward, gainedReward, predictedReward);
        this.priorProbability = priorProbability;
        this.totalActionValue = predictedReward;
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
