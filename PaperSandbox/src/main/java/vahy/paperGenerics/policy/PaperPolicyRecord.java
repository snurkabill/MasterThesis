package vahy.paperGenerics.policy;

import vahy.api.policy.PolicyRecordBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaperPolicyRecord extends PolicyRecordBase {

    private final double[] priorProbabilities;
    private final double predictedRisk;
    private final double totalRiskAllowed;
    private final int expandedNodeCountSoFar;

    public PaperPolicyRecord(double[] priorProbabilities,
                             double[] policyProbabilities,
                             double predictedReward,
                             double predictedRisk,
                             double totalRiskAllowed,
                             int expandedNodeCountSoFar) {
        super(policyProbabilities, predictedReward);
        this.priorProbabilities = priorProbabilities;
        this.predictedRisk = predictedRisk;
        this.totalRiskAllowed = totalRiskAllowed;
        this.expandedNodeCountSoFar = expandedNodeCountSoFar;
    }

    public double[] getPriorProbabilities() {
        return priorProbabilities;
    }

    public double getPredictedRisk() {
        return predictedRisk;
    }

    public double getTotalRiskAllowed() {
        return totalRiskAllowed;
    }

    @Override
    public String toString() {
        return "PaperPolicyRecord{" +
                "priorProbabilities=" + Arrays.toString(priorProbabilities) +
                ", riskPredicted=" + predictedRisk +
                ", totalRiskAllowed=" + totalRiskAllowed +
                super.toString() +
                '}';
    }

    public List<String> getCsvHeader() {
        var list = new ArrayList<>(super.getCsvHeader());
        for (int i = 0; i < priorProbabilities.length; i++) {
            list.add("PriorProbabilities_" + i);
        }
        list.add("Predicted risk");
        list.add("Total risk allowed");
        list.add("Expanded nodes so far");
        return list;
    }

    public List<String> getCsvRecord() {
        var list = new ArrayList<>(super.getCsvRecord());
        for (int i = 0; i < priorProbabilities.length; i++) {
            list.add(Double.toString(priorProbabilities[i]));
        }
        list.add(Double.toString(predictedRisk));
        list.add(Double.toString(totalRiskAllowed));
        list.add(Integer.toString(expandedNodeCountSoFar));
        return list;
    }
}
