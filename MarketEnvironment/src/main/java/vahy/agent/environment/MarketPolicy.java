package vahy.agent.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecordBase;
import vahy.environment.MarketAction;
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;

public abstract class MarketPolicy implements Policy<MarketAction, DoubleVector, MarketProbabilities, MarketState, PolicyRecordBase> {

}
