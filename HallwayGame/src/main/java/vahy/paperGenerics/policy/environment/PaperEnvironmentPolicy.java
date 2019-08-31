package vahy.paperGenerics.policy.environment;

import vahy.environment.HallwayAction;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.policy.PaperPolicy;

import java.util.SplittableRandom;

public class PaperEnvironmentPolicy extends EnvironmentPolicy implements PaperPolicy<HallwayAction,  DoubleVector, EnvironmentProbabilities, HallwayStateImpl> {

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

}
