package vahy.paperGenerics;

import com.quantego.clp.CLPVariable;
import vahy.api.model.Action;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;

import java.util.Map;

public class PaperMetadata<TAction extends Action, TReward extends DoubleScalarReward> extends MonteCarloTreeSearchMetadata<TReward> {

    private final Map<TAction, Double> childPriorProbabilities;
    private CLPVariable nodeProbabilityFlow;
    private final double priorProbability;
    private double predictedRisk;

    public PaperMetadata(TReward cumulativeReward,
                         TReward gainedReward,
                         TReward predictedReward,
                         double priorProbability,
                         double predictedRisk,
                         Map<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, predictedReward);
        this.priorProbability = priorProbability;
        this.predictedRisk = predictedRisk;
        this.childPriorProbabilities = childPriorProbabilities;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public double getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(double predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public CLPVariable getNodeProbabilityFlow() {
        return nodeProbabilityFlow;
    }

    public void setNodeProbabilityFlow(CLPVariable nodeProbabilityFlow) {
        this.nodeProbabilityFlow = nodeProbabilityFlow;
    }

    public Map<TAction, Double> getChildPriorProbabilities() {
        return childPriorProbabilities;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append("\\nPriorProbability: ");
        stringBuilder.append(this.priorProbability);
        stringBuilder.append("\\nPredictedRisk: ");
        stringBuilder.append(this.predictedRisk);
        stringBuilder.append("\\nCalculatedFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? this.nodeProbabilityFlow.getSolution() : null);
        return stringBuilder.toString();
    }

}
