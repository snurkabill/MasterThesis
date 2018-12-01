package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;

import java.util.Map;

public class AlphaGoNodeMetadata<TAction extends Action, TReward extends Reward> extends MonteCarloTreeSearchMetadata<TReward> {

    private double priorProbability; /// P value
    private final Map<TAction, Double> childPriorProbabilities;

    public AlphaGoNodeMetadata(TReward cumulativeReward, TReward gainedReward, TReward predictedReward, double priorProbability, Map<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, predictedReward);
        this.priorProbability = priorProbability;
        this.childPriorProbabilities = childPriorProbabilities;
    }

    public Map<TAction, Double> getChildPriorProbabilities() {
        return childPriorProbabilities;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append("\\nPriorProbability: ");
        stringBuilder.append(this.priorProbability);
        return stringBuilder.toString();
    }
}
