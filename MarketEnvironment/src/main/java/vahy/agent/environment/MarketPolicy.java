package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.policy.PaperPolicy;

public abstract class MarketPolicy implements PaperPolicy<MarketAction, DoubleVector, MarketProbabilities, MarketState> {

}
