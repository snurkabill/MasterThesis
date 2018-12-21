package vahy.environment;

public class TradingSystemParameters {

    private final double systemStopLoss;
    private final double instantCommissionWhenTradeOpens;
    private final double smallestValueDiff;

    public TradingSystemParameters(double systemStopLoss, double instantCommissionWhenTradeOpens, double smallestValueDiff) {
        if(systemStopLoss < 0.0) {
            throw new IllegalArgumentException("StopLoss must be positive");
        }
        if(instantCommissionWhenTradeOpens < 0.0) {
            throw new IllegalArgumentException("InstantCommission must be positive");
        }
        if(smallestValueDiff <= 0.0) {
            throw new IllegalArgumentException("SmallestValueDiff ust be non negative");
        }
        this.smallestValueDiff = smallestValueDiff;
        this.systemStopLoss = systemStopLoss;
        this.instantCommissionWhenTradeOpens = instantCommissionWhenTradeOpens;
    }

    public double getSystemStopLoss() {
        return systemStopLoss;
    }

    public double getInstantCommissionWhenTradeOpens() {
        return instantCommissionWhenTradeOpens;
    }

    public double getSmallestValueDiff() {
        return smallestValueDiff;
    }
}
