package vahy.paperGenerics.metadata;

import com.quantego.clp.CLPVariable;
import vahy.api.model.Action;
import vahy.impl.policy.mcts.MonteCarloTreeSearchMetadata;

import java.util.EnumMap;
import java.util.Map;

public class PaperMetadata<TAction extends Enum<TAction> & Action> extends MonteCarloTreeSearchMetadata {

    private final EnumMap<TAction, Double> childPriorProbabilities;
    private CLPVariable nodeProbabilityFlow;
    private double priorProbability;
    private double predictedRisk;
    private double sumOfRisk;
    private double flow;

    public PaperMetadata(double cumulativeReward,
                         double gainedReward,
                         double predictedReward,
                         double priorProbability,
                         double predictedRisk,
                         EnumMap<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, predictedReward);
        this.priorProbability = priorProbability;
        this.predictedRisk = predictedRisk;
        this.childPriorProbabilities = childPriorProbabilities;
        this.sumOfRisk = predictedRisk;
        this.flow = 0.0;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public void setPriorProbability(double priorProbability) {
        this.priorProbability = priorProbability;
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

    public double getFlow() {
        if(nodeProbabilityFlow == null) {
            return flow;
        } else {
            return nodeProbabilityFlow.getSolution();
        }
    }

    public void setFlow(double flow) {
        if(nodeProbabilityFlow != null) {
            throw new IllegalStateException("Variable for flow optimization exists");
        }
        this.flow = flow;
    }

    public void setNodeProbabilityFlow(CLPVariable nodeProbabilityFlow) {
        this.nodeProbabilityFlow = nodeProbabilityFlow;
    }

    public Map<TAction, Double> getChildPriorProbabilities() {
        return childPriorProbabilities;
    }

    public double getSumOfRisk() {
        return sumOfRisk;
    }

    public void setSumOfRisk(double sumOfRisk) {
        this.sumOfRisk = sumOfRisk;
    }

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append(System.lineSeparator()).append("nPriorProbability: ");
        stringBuilder.append(this.priorProbability);
        stringBuilder.append(System.lineSeparator()).append("nPredictedRisk: ");
        stringBuilder.append(this.predictedRisk);
        stringBuilder.append(System.lineSeparator()).append("nSumOfPredictedRisk: ");
        stringBuilder.append(this.sumOfRisk);
        stringBuilder.append(System.lineSeparator()).append("nCalculatedFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? this.nodeProbabilityFlow.getSolution() : null);
        stringBuilder.append(System.lineSeparator()).append("nFinalFlow: ");
        stringBuilder.append(getFlow());
        return stringBuilder.toString();
    }
}
