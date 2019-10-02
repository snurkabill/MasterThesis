package vahy.paperGenerics.reinforcement.episode;

import vahy.api.policy.PolicyRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaperPolicyStepRecord implements PolicyRecord {

    private final double[] priorProbabilities;
    private final double[] policyProbabilities;
    private final double predictedReward;
    private final double predictedRisk;
    private final double totalRiskAllowed;

    public PaperPolicyStepRecord(double[] priorProbabilities, double[] policyProbabilities, double predictedReward, double predictedRisk, double totalRiskAllowed) {
        this.priorProbabilities = priorProbabilities;
        this.policyProbabilities = policyProbabilities;
        this.predictedReward = predictedReward;
        this.predictedRisk = predictedRisk;
        this.totalRiskAllowed = totalRiskAllowed;
    }

    public double[] getPriorProbabilities() {
        return priorProbabilities;
    }

    public double[] getPolicyProbabilities() {
        return policyProbabilities;
    }

    public double getPredictedReward() {
        return predictedReward;
    }

    public double getPredictedRisk() {
        return predictedRisk;
    }

    public double getTotalRiskAllowed() {
        return totalRiskAllowed;
    }

    @Override
    public String toString() {
        return "PolicyStepRecord{" +
                "priorProbabilities=" + Arrays.toString(priorProbabilities) +
                ", policyProbabilities=" + Arrays.toString(policyProbabilities) +
                ", predictedReward=" + predictedReward +
                ", riskPredicted=" + predictedRisk +
                ", totalRiskAllowed=" + totalRiskAllowed +
                '}';
    }

    public List<String> getCsvHeader() {
        var list = new ArrayList<String>();
        for (int i = 0; i < priorProbabilities.length; i++) {
            list.add("PriorProbabilities_" + i);
        }
        for (int i = 0; i < policyProbabilities.length; i++) {
            list.add("PolicyProbabilities_" + i);
        }
        list.add("Predicted reward");
        list.add("Predicted risk");
        list.add("Total risk allowed");
        return list;
    }

    public List<String> getCsvRecord() {
        var list = new ArrayList<String>();
        for (int i = 0; i < priorProbabilities.length; i++) {
            list.add(Double.toString(priorProbabilities[i]));
        }
        for (int i = 0; i < policyProbabilities.length; i++) {
            list.add(Double.toString(policyProbabilities[i]));
        }
        list.add(Double.toString(predictedReward));
        list.add(Double.toString(predictedRisk));
        list.add(Double.toString(totalRiskAllowed));
        return list;
    }
}
