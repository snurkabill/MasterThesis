package vahy;

import vahy.api.experiment.ProblemConfig;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;

public class MarketConfig extends ProblemConfig {

    private final MarketEnvironmentStaticPart marketEnvironmentStaticPart;
    private final int lookbackLength;
    private final int allowedCountOfTimestampsAheadOfEndOfData;

    public MarketConfig(int maximalStepCountBound,
                        MarketEnvironmentStaticPart marketEnvironmentStaticPart,
                        int lookbackLength,
                        MarketDataProvider marketDataProvider,
                        int allowedCountOfTimestampsAheadOfEndOfData) {
        super(maximalStepCountBound, false);
        this.marketEnvironmentStaticPart = marketEnvironmentStaticPart;
        this.lookbackLength = lookbackLength;
        this.allowedCountOfTimestampsAheadOfEndOfData = allowedCountOfTimestampsAheadOfEndOfData;
    }

    public MarketEnvironmentStaticPart getMarketEnvironmentStaticPart() {
        return marketEnvironmentStaticPart;
    }

    public int getLookbackLength() {
        return lookbackLength;
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
