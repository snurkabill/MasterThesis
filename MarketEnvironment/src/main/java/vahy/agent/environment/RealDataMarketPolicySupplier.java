package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketState;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;

public class RealDataMarketPolicySupplier extends PaperPolicySupplier<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> {

    private final MarketDataProvider marketDataProvider;

    public RealDataMarketPolicySupplier(MarketDataProvider marketDataProvider) {
        super(null, null, 0.0, null, null, null, null, null, null);
        this.marketDataProvider = marketDataProvider;
    }

    @Override
    public PaperPolicy<MarketAction, DoubleVector, DoubleVector, MarketState> initializePolicy(MarketState initialState) {
        int index = initialState.getCurrentDataIndex();
        return new RealDataMarketPolicy(marketDataProvider.getMarketMovementArray(), index + 1);
    }
}
