package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.search.node.ProbabilisticNodeMetadata;
import vahy.impl.search.MCTS.MonteCarloTreeMetadata;

import java.util.EnumMap;
import java.util.Map;

public class AlphaGoNodeMetadata<TAction extends Enum<TAction> & Action> extends MonteCarloTreeMetadata implements ProbabilisticNodeMetadata<TAction> {

    private final double priorProbability; /// P value
    private final EnumMap<TAction, Double> childPriorProbabilities;

    public AlphaGoNodeMetadata(double[] cumulativeReward, double[] gainedReward, double priorProbability, EnumMap<TAction, Double> childPriorProbabilities) {
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
        stringBuilder.append(System.lineSeparator()).append("PriorProbability: ");
        stringBuilder.append(this.priorProbability);
        return stringBuilder.toString();
    }
}
