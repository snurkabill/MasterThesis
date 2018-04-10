package vahy.environment.agent.policy;

import vahy.environment.state.ImmutableStateImpl;

public interface IPolicy {

    double[] getActionProbabilityDistribution(ImmutableStateImpl gameState);

}
