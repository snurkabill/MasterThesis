package vahy.environment.agent.policy;

import vahy.api.model.State;

public interface IPolicy {

    double[] getActionProbabilityDistribution(State gameState);

}
