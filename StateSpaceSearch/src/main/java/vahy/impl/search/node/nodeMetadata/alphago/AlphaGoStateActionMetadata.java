package vahy.impl.search.node.nodeMetadata.alphago;

import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;

public class AlphaGoStateActionMetadata<TReward extends Reward> extends AbstractStateActionMetadata<TReward> {

    private final double priorProbability; /// P value
    private int visitCount; // N value
    private double meanActionValue; // Q value
    private double totalActionValue; // W value

    public AlphaGoStateActionMetadata(TReward gainedReward, double priorProbability) {
        super(gainedReward);
        this.priorProbability = priorProbability;
        this.visitCount = 0;
        this.meanActionValue = 0;
        this.totalActionValue = 0;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public double getMeanActionValue() {
        return meanActionValue;
    }

    public void setMeanActionValue(double meanActionValue) {
        this.meanActionValue = meanActionValue;
    }

    public double getTotalActionValue() {
        return totalActionValue;
    }

    public void setTotalActionValue(double totalActionValue) {
        this.totalActionValue = totalActionValue;
    }
}
