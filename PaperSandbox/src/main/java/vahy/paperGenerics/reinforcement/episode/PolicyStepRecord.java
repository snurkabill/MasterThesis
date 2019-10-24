package vahy.paperGenerics.reinforcement.episode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolicyStepRecord {

    private final double[] priorProbabilities;
    private final double[] policyProbabilities;
    private final double rewardPredicted;
    private final double risk;

    public PolicyStepRecord(double[] priorProbabilities, double[] policyProbabilities, double rewardPredicted, double risk) {
        this.priorProbabilities = priorProbabilities;
        this.policyProbabilities = policyProbabilities;
        this.rewardPredicted = rewardPredicted;
        this.risk = risk;
    }

    public double[] getPriorProbabilities() {
        return priorProbabilities;
    }

    public double[] getPolicyProbabilities() {
        return policyProbabilities;
    }

    public double getRewardPredicted() {
        return rewardPredicted;
    }

    public double getRisk() {
        return risk;
    }

    @Override
    public String toString() {
        return "PolicyStepRecord{" +
                "priorProbabilities=" + Arrays.toString(priorProbabilities) +
                ", policyProbabilities=" + Arrays.toString(policyProbabilities) +
                ", rewardPredicted=" + rewardPredicted +
                ", risk=" + risk +
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
        list.add(Double.toString(rewardPredicted));
        list.add(Double.toString(risk));
        return list;
    }
}
