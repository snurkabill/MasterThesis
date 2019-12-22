package vahy.original.environment.agent.policy.environment;

import vahy.impl.model.observation.DoubleVector;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.state.EnvironmentProbabilities;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class PaperEnvironmentPolicy extends EnvironmentPolicy<PaperPolicyRecord> implements PaperPolicy<HallwayAction,  DoubleVector, EnvironmentProbabilities, HallwayStateImpl> {

    public PaperEnvironmentPolicy(SplittableRandom random) {
        super(random);
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(HallwayStateImpl gameState) {
        return this.getActionProbabilityDistribution(gameState);
    }

    @Override
    public double getEstimatedReward(HallwayStateImpl gameState) {
        return 0.0d;
    }

    @Override
    public double getEstimatedRisk(HallwayStateImpl gameState) {
        return 0;
    }

    @Override
    public double getInnerRiskAllowed() {
        return 0;
    }

    @Override
    public PaperPolicyRecord getPolicyRecord(HallwayStateImpl gameState) {
        var probs = this.getActionProbabilityDistribution(gameState);
        return new PaperPolicyRecord(probs, probs, 0.0, 0.0, 0.0, 0);
    }
}
