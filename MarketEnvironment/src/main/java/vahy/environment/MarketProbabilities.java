package vahy.environment;

import vahy.api.model.observation.FixedModelObservation;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;

public class MarketProbabilities implements FixedModelObservation<MarketAction> {

    private static double[] staticRandomMarketProbabilities = new double[] {0.5, 0.5};
    private static DoubleVector wrappedInVector = new DoubleVector(staticRandomMarketProbabilities);

    private final MarketDataProvider marketDataProvider;

    private static ImmutableTuple<List<MarketAction>, List<Double>> probabilities = new ImmutableTuple<>(Arrays.asList(MarketAction.UP, MarketAction.DOWN), Arrays.asList(0.5, 0.5));

    public MarketProbabilities(MarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    @Override
    public ImmutableTuple<List<MarketAction>, List<Double>> getProbabilities() {
        return probabilities;
    }

    public MarketDataProvider getMarketDataProvider() {
        return marketDataProvider;
    }
}
