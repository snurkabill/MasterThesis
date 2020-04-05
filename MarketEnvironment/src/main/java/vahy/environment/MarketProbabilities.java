package vahy.environment;

import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;

public class MarketProbabilities implements Observation {

    private static double[] staticRandomMarketProbabilities = new double[] {0.5, 0.5};
    private static DoubleVector wrappedInVector = new DoubleVector(staticRandomMarketProbabilities);

    private final MarketDataProvider marketDataProvider;

    public MarketProbabilities(MarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    public MarketDataProvider getMarketDataProvider() {
        return marketDataProvider;
    }
}
