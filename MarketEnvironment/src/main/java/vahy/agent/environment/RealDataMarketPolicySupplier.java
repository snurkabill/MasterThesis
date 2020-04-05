package vahy.agent.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.environment.MarketAction;
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.AbstractRandomizedPolicySupplier;

import java.util.SplittableRandom;

public class RealDataMarketPolicySupplier extends AbstractRandomizedPolicySupplier<MarketAction, DoubleVector, MarketProbabilities, MarketState, PolicyRecordBase> {

    public RealDataMarketPolicySupplier(SplittableRandom random) {
        super(random);
    }

    @Override
    protected Policy<MarketAction, DoubleVector, MarketProbabilities, MarketState, PolicyRecordBase> initializePolicy_inner(MarketState initialState, PolicyMode policyMode, SplittableRandom random) {
        return new RealDataMarketPolicy(initialState.getOpponentObservation().getMarketDataProvider(), initialState.getCurrentDataIndex() + 1);
    }

}
