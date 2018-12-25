package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.policy.PaperPolicy;

public abstract class MarketPolicy implements PaperPolicy<MarketAction, DoubleReward, DoubleVector, DoubleVector, MarketState> {

}
