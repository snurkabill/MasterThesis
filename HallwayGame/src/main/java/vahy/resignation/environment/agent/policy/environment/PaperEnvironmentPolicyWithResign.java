package vahy.resignation.environment.agent.policy.environment;

import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.state.EnvironmentProbabilities;
import vahy.resignation.environment.state.HallwayStateWithResign;

import java.util.SplittableRandom;

public class PaperEnvironmentPolicyWithResign extends EnvironmentPolicy<PaperPolicyRecord> implements PaperPolicy<HallwayActionWithResign,  DoubleVector, EnvironmentProbabilities, HallwayStateWithResign> {

    public PaperEnvironmentPolicyWithResign(SplittableRandom random) {
        super(random);
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(HallwayStateWithResign gameState) {
        return this.getActionProbabilityDistribution(gameState);
    }

    @Override
    public double getEstimatedReward(HallwayStateWithResign gameState) {
        return 0.0d;
    }

    @Override
    public double getEstimatedRisk(HallwayStateWithResign gameState) {
        return 0;
    }

    @Override
    public double getInnerRiskAllowed() {
        return 0;
    }

    @Override
    public PaperPolicyRecord getPolicyRecord(HallwayStateWithResign gameState) {
        var probs = this.getActionProbabilityDistribution(gameState);
        return new PaperPolicyRecord(probs, probs, 0.0, 0.0, 0.0, 0);
    }
}
