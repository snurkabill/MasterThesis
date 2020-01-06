package vahy.api.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolicyRecordBase implements PolicyRecord {

    private final double[] policyProbabilities;
    private final double predictedReward;

    public PolicyRecordBase(double[] policyProbabilities, double predictedReward) {
        this.policyProbabilities = policyProbabilities;
        this.predictedReward = predictedReward;
    }

    @Override
    public double[] getPolicyProbabilities() {
        return policyProbabilities;
    }

    @Override
    public double getPredictedReward() {
        return predictedReward;
    }

    @Override
    public List<String> getCsvHeader() {
        var list = new ArrayList<String>();
        for (int i = 0; i < policyProbabilities.length; i++) {
            list.add("PolicyProbabilities_" + i);
        }
        list.add("Predicted reward");
        return list;
    }

    @Override
    public List<String> getCsvRecord() {
        var list = new ArrayList<String>();
        for (int i = 0; i < policyProbabilities.length; i++) {
            list.add(Double.toString(policyProbabilities[i]));
        }
        list.add(Double.toString(predictedReward));
        return list;
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Predicted reward: [");
        sb.append(predictedReward);
        sb.append("], PredictedPolicices: [");
        sb.append(Arrays.toString(policyProbabilities));
        sb.append("].");
        return sb.toString();
    }
}
