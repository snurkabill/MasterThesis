package vahy;

import vahy.api.episode.InitialStateSupplier;
import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.MarketState;
import vahy.environment.TradingSystemState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

import java.util.SplittableRandom;

public class InitialMarketStateSupplier implements InitialStateSupplier<MarketAction, DoubleReward, DoubleVector, MarketState> {

    private final SplittableRandom random;
    private final MarketEnvironmentStaticPart marketEnvironmentStaticPart;
    private final int lookbackLength;
    private final MarketDataProvider marketDataProvider;

    public InitialMarketStateSupplier(SplittableRandom random, MarketEnvironmentStaticPart marketEnvironmentStaticPart, int lookbackLength, MarketDataProvider marketDataProvider) {
        this.random = random;
        this.marketEnvironmentStaticPart = marketEnvironmentStaticPart;
        this.lookbackLength = lookbackLength;
        this.marketDataProvider = marketDataProvider;
    }

    @Override
    public MarketState createInitialState() {
        int index = random.nextInt(lookbackLength, marketDataProvider.getMarketMovementArray().length);
        double[] lookback = new double[lookbackLength];
        System.arraycopy(marketDataProvider.getMarketMidPriceArray(), index - lookbackLength, lookback, 0, lookbackLength);
        int maxIndex = marketDataProvider.getMarketMidPriceArray().length - 1;
        int indexDiff = maxIndex - index;
        return new MarketState(
            // TradingSystemState.values()[random.nextInt(TradingSystemState.values().length)],
            TradingSystemState.NO_POSITION,
            marketEnvironmentStaticPart,
            lookback,
            marketDataProvider.getMarketMidPriceArray()[index],
            marketDataProvider.getMarketMovementArray()[index],
            index,
            indexDiff
            );
    }
}
