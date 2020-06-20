package vahy.impl.search.alphazero;

import vahy.api.model.Action;
import vahy.api.search.node.ProbabilisticNodeMetadata;
import vahy.impl.search.mcts.MCTSMetadata;

import java.util.EnumMap;

public class AlphaZeroNodeMetadata<TAction extends Enum<TAction> & Action> extends MCTSMetadata implements ProbabilisticNodeMetadata<TAction> {

    private final double priorProbability; /// P value
    private final EnumMap<TAction, Double> childPriorProbabilities;

    public AlphaZeroNodeMetadata(double[] cumulativeReward, double[] gainedReward, double priorProbability, EnumMap<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward);
        this.priorProbability = priorProbability;
        this.childPriorProbabilities = childPriorProbabilities;
    }

    @Override
    public EnumMap<TAction, Double> getChildPriorProbabilities() {
        return childPriorProbabilities;
    }

    @Override
    public double getPriorProbability() {
        return priorProbability;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append("\\n").append("PriorProbability: ");
        stringBuilder.append(this.priorProbability);
        return stringBuilder.toString();
    }
}
