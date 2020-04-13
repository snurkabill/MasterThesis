package vahy;

import vahy.api.policy.PolicyMode;
import vahy.environment.MarketAction;
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.environment.TradingSystemState;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.SplittableRandom;

public class InitialMarketStateSupplier extends AbstractInitialStateSupplier<MarketConfig, MarketAction, DoubleVector, MarketProbabilities, MarketState> {

    public InitialMarketStateSupplier(MarketConfig marketConfig, SplittableRandom random) {
        super(marketConfig, random);
    }

    @Override
    protected MarketState createState_inner(MarketConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        var lookbackLength = problemConfig.getLookbackLength();
        var marketDataProvider = problemConfig.getMarketEnvironmentStaticPart().getMarketDataProvider();
        var marketEnvironmentStaticPart = problemConfig.getMarketEnvironmentStaticPart();
        int index = random.nextInt(lookbackLength, marketDataProvider.getMarketMovementArray().length - problemConfig.getAllowedCountOfTimestampsAheadOfEndOfData());
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
