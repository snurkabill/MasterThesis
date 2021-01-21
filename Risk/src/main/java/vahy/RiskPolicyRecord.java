package vahy;

import vahy.api.policy.PolicyRecordBase;

import java.util.ArrayList;
import java.util.List;

public class RiskPolicyRecord extends PolicyRecordBase {

    private final double predictedRisk;
    private final double totalRiskAllowed;
    private final int expandedNodeCountSoFar;

    public RiskPolicyRecord(double[] policyProbabilities,
                            double predictedReward,
                            double predictedRisk,
                            double totalRiskAllowed,
                            int expandedNodeCountSoFar) {
        super(policyProbabilities, predictedReward);
        this.predictedRisk = predictedRisk;
        this.totalRiskAllowed = totalRiskAllowed;
        this.expandedNodeCountSoFar = expandedNodeCountSoFar;
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
                ", riskPredicted=" + predictedRisk +
                ", totalRiskAllowed=" + totalRiskAllowed +
                super.toString() +
                '}';
    }

    @Override
    public List<String> getCsvHeader() {
        var list = new ArrayList<>(super.getCsvHeader());
        list.add("Predicted risk");
        list.add("Total risk allowed");
        list.add("Expanded nodes so far");
        return list;
    }

    @Override
    public List<String> getCsvRecord() {
        var list = new ArrayList<>(super.getCsvRecord());
        list.add(Double.toString(predictedRisk));
        list.add(Double.toString(totalRiskAllowed));
        list.add(Integer.toString(expandedNodeCountSoFar));
        return list;
    }
}
