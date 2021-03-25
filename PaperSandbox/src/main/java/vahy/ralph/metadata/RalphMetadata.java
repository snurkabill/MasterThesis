package vahy.ralph.metadata;

import com.quantego.clp.CLPVariable;
import vahy.api.model.Action;
import vahy.impl.policy.alphazero.AlphaZeroNodeMetadata;

import java.util.Arrays;
import java.util.EnumMap;

public class RalphMetadata<TAction extends Enum<TAction> & Action> extends AlphaZeroNodeMetadata<TAction> {

    private CLPVariable nodeProbabilityFlow;
    private final double[] expectedRisk;
    private final double[] sumOfRisk;
    private double flow;

    public RalphMetadata(double[] cumulativeReward, double[] gainedReward, double priorProbability, double[] expectedRisk, EnumMap<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, priorProbability, childPriorProbabilities);
        this.expectedRisk = expectedRisk;
        this.sumOfRisk = Arrays.copyOf(expectedRisk, expectedRisk.length);
        this.flow = 0.0;
    }

    public double[] getExpectedRisk() {
        return expectedRisk;
    }

    public double[] getSumOfRisk() {
        return sumOfRisk;
    }

    public CLPVariable getNodeProbabilityFlow() {
        return nodeProbabilityFlow;
    }

    public double getFlow() {
        return flow;
    }

    public void afterSolution() {
        if(nodeProbabilityFlow != null) {
            flow = nodeProbabilityFlow.getSolution();
            nodeProbabilityFlow = null;
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

    @Override
    public String toString() {
        String baseString = super.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseString);
        stringBuilder.append("\\n").append("ExpectedRisk: ");
        stringBuilder.append(Arrays.toString(expectedRisk));
        stringBuilder.append("\\n").append("SumOfRisk: ");
        stringBuilder.append(Arrays.toString(sumOfRisk));
        stringBuilder.append("\\n").append("CalculatedFlow: ");
        stringBuilder.append(nodeProbabilityFlow != null ? this.nodeProbabilityFlow.getSolution() : null);
        stringBuilder.append("\\n").append("FinalFlow: ");
        stringBuilder.append(getFlow());
        return stringBuilder.toString();
    }
}
