package vahy.paperGenerics.metadata;

import com.quantego.clp.CLPVariable;
import vahy.api.model.Action;
import vahy.impl.policy.alphazero.AlphaZeroNodeMetadata;

import java.util.EnumMap;

public class PaperMetadata<TAction extends Enum<TAction> & Action> extends AlphaZeroNodeMetadata<TAction> {

    private CLPVariable nodeProbabilityFlow;
    private double expectedRisk;
    private double sumOfRisk;
    private double flow;

    public PaperMetadata(double[] cumulativeReward,
                         double[] gainedReward,
                         double priorProbability,
                         double expectedRisk,
                         EnumMap<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, priorProbability, childPriorProbabilities);
        this.expectedRisk = expectedRisk;
        this.sumOfRisk = expectedRisk;
        this.flow = 0.0;
    }

    public double getExpectedRisk() {
        return expectedRisk;
    }

    public void setExpectedRisk(double expectedRisk) {
        this.expectedRisk = expectedRisk;
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
        stringBuilder.append("\\n").append("nPredictedRisk: ");
        stringBuilder.append(this.expectedRisk);
        stringBuilder.append("\\n").append("nSumOfPredictedRisk: ");
        stringBuilder.append(this.sumOfRisk);
        stringBuilder.append("\\n").append("nCalculatedFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? this.nodeProbabilityFlow.getSolution() : null);
        stringBuilder.append("\\n").append("nFinalFlow: ");
        stringBuilder.append(getFlow());
        return stringBuilder.toString();
    }
}
