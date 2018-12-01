package vahy.paper.reinforcement.episode;

import vahy.impl.model.reward.DoubleReward;

public class StepRecord {

    private final double[] priorProbabilities;
    private final double[] policyProbabilities;
    private final DoubleReward rewardPredicted;
    private final double risk;

    public StepRecord(double[] priorProbabilities, double[] policyProbabilities, DoubleReward rewardPredicted, double risk) {
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

    public DoubleReward getRewardPredicted() {
        return rewardPredicted;
    }

    public double getRisk() {
        return risk;
    }
}
