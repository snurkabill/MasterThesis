package vahy.environment.agent.policy;

public interface IPolicy {

    double[] getActionProbabilityDistribution(IState gameState);

}
