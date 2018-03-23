package vahy.environment.agent.policy;

import vahy.environment.state.IState;

public interface IPolicy {

    double[] getActionProbabilityDistribution(IState gameState);

}
