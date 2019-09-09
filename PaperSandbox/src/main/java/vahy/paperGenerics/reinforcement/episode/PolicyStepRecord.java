package vahy.paperGenerics.reinforcement.episode;

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
}
