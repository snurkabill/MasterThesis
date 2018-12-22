package vahy.agent.environment;

import vahy.api.policy.Policy;
import vahy.environment.MarketAction;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public abstract class MarketPolicy implements Policy<MarketAction, DoubleReward, DoubleVector, MarketState> {

}
