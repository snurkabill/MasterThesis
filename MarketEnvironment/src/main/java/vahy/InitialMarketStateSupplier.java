package vahy;

import vahy.api.episode.InitialStateSupplier;
import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.environment.TradingSystemState;
import vahy.impl.model.observation.DoubleVector;

import java.util.SplittableRandom;

public class InitialMarketStateSupplier implements InitialStateSupplier<MarketAction, DoubleVector, DoubleVector,  MarketState> {

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
        RealMarketAction[] direction = new RealMarketAction[lookbackLength];
        double[] lookback = new double[lookbackLength];

        for (int i = index - lookbackLength, j = 0; i < index; i++, j++) {
            direction[j] = marketDataProvider.getMarketMovementArray()[i];
        }
        for (int i = lookbackLength - 1; i >= 0; i--) {
            if(i == lookbackLength - 1) {
                lookback[i] = 0;
            } else {
                lookback[i] = (direction[i + 1] == RealMarketAction.MARKET_UP ? -1 : 1) + lookback[i + 1];
            }
        }

//        System.arraycopy(marketDataProvider.getMarketMidPriceArray(), index - lookbackLength, lookback, 0, lookbackLength);
        int maxIndex = marketDataProvider.getMarketMidPriceArray().length - 1;
        int indexDiff = maxIndex - index;
        return new MarketState(
            // TradingSystemState.values()[random.nextInt(TradingSystemState.values().length)],
            TradingSystemState.NO_POSITION,
            marketEnvironmentStaticPart,
            lookback,
            marketDataProvider.getMarketMidPriceArray()[index - 1],
            marketDataProvider.getMarketMovementArray()[index - 1],
            index,
            indexDiff
            );
    }
}
