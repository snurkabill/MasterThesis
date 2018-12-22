package vahy.environment;

public class MarketDataProvider {

    private final RealMarketAction[] marketMovementArray;
    private final double[] marketMidPriceArray;

    public MarketDataProvider(RealMarketAction[] marketMovementArray, double[] marketMidPriceArray) {
        if(marketMidPriceArray.length != marketMovementArray.length) {
            throw new IllegalArgumentException("History lengths of prices and movements differs");
        }
        this.marketMovementArray = marketMovementArray;
        this.marketMidPriceArray = marketMidPriceArray;
    }

    public RealMarketAction[] getMarketMovementArray() {
        return marketMovementArray;
    }

    public double[] getMarketMidPriceArray() {
        return marketMidPriceArray;
    }
}
