package vahy.agent.environment;

import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.environment.MarketAction;
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.AbstractRandomizedPolicySupplier;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.SplittableRandom;

public class RealDataMarketPolicySupplier extends AbstractRandomizedPolicySupplier<MarketAction, DoubleVector, MarketProbabilities, MarketState, PaperPolicyRecord> {

    public RealDataMarketPolicySupplier(SplittableRandom random) {
        super(random);
    }

//    public RealDataMarketPolicySupplier(MarketDataProvider marketDataProvider) {
//        super(null, null, 0.0, null, null, null, null, null, null, null, null, null);
//        this.marketDataProvider = marketDataProvider;
//    }

    @Override
    protected Policy<MarketAction, DoubleVector, MarketProbabilities, MarketState, PaperPolicyRecord> initializePolicy_inner(MarketState initialState, PolicyMode policyMode, SplittableRandom random) {
        return new RealDataMarketPolicy(initialState.getOpponentObservation().getMarketDataProvider(), initialState.getCurrentDataIndex() + 1);
    }


//    @Override
//    public PaperPolicy<MarketAction, DoubleVector, DoubleVector, MarketState> initializePolicy(MarketState initialState) {
//        int index = initialState.getCurrentDataIndex();
//        return new RealDataMarketPolicy(marketDataProvider.getMarketMovementArray(), index + 1);
//    }
}
