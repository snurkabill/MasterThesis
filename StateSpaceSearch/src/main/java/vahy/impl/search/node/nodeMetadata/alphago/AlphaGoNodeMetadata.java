package vahy.impl.search.node.nodeMetadata.alphago;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;

import java.util.Map;

public class AlphaGoNodeMetadata<TAction extends Action, TReward extends Reward> extends AbstractSearchNodeMetadata<TAction, TReward, AlphaGoStateActionMetadata<TReward>> {

    private double winningProbability; // in article V value
    private int totalVisitCounter; // sum over all b : N(s, b)

    public AlphaGoNodeMetadata(TReward cumulativeReward, Map<TAction, AlphaGoStateActionMetadata<TReward>> stateActionMetadataMap) {
        super(cumulativeReward, stateActionMetadataMap);
        this.winningProbability = 0;
        this.totalVisitCounter = 0;
    }

    public double getWinningProbability() {
        return winningProbability;
    }

    public void setWinningProbability(double winningProbability) {
        this.winningProbability = winningProbability;
    }

    public int getTotalVisitCounter() {
        return totalVisitCounter;
    }

    public void setTotalVisitCounter(int totalVisitCounter) {
        this.totalVisitCounter = totalVisitCounter;
    }
}
