package vahy;

import vahy.api.experiment.ProblemConfig;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;

public class MarketConfig implements ProblemConfig {

    private final MarketEnvironmentStaticPart marketEnvironmentStaticPart;
    private final int lookbackLength;
    private final MarketDataProvider marketDataProvider;
    private final int allowedCountOfTimestampsAheadOfEndOfData;

    public MarketConfig(MarketEnvironmentStaticPart marketEnvironmentStaticPart, int lookbackLength, MarketDataProvider marketDataProvider, int allowedCountOfTimestampsAheadOfEndOfData) {
        this.marketEnvironmentStaticPart = marketEnvironmentStaticPart;
        this.lookbackLength = lookbackLength;
        this.marketDataProvider = marketDataProvider;
        this.allowedCountOfTimestampsAheadOfEndOfData = allowedCountOfTimestampsAheadOfEndOfData;
    }

    public MarketEnvironmentStaticPart getMarketEnvironmentStaticPart() {
        return marketEnvironmentStaticPart;
    }

    public int getLookbackLength() {
        return lookbackLength;
    }

    public MarketDataProvider getMarketDataProvider() {
        return marketDataProvider;
    }

    public int getAllowedCountOfTimestampsAheadOfEndOfData() {
        return allowedCountOfTimestampsAheadOfEndOfData;
    }

    @Override
    public String toLog() {
        return null;
    }

    @Override
    public String toFile() {
        return null;
    }
}
