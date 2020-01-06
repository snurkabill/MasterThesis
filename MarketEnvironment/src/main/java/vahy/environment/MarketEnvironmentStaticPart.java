package vahy.environment;

public class MarketEnvironmentStaticPart {

    private final double systemStopLoss;
    private final double constantSpread;
    private final double priceRange;
    private final int size;
    private final double commission;

    public MarketEnvironmentStaticPart(double systemStopLoss, double constantSpread, double priceRange, int size, double commission) {
        this.size = size;
        this.commission = commission;
        if(systemStopLoss < 0.0) {
            throw new IllegalArgumentException("StopLoss must be positive");
        }
        if(constantSpread < 0.0) {
            throw new IllegalArgumentException("ConstantSpread must be positive");
        }
        if(priceRange <= 0.0) {
            throw new IllegalArgumentException("PriceRange ust be non negative");
        }
        this.priceRange= priceRange;
        this.systemStopLoss = systemStopLoss;
        this.constantSpread = constantSpread;
    }

    public double getSystemStopLoss() {
        return systemStopLoss;
    }

    public double getConstantSpread() {
        return constantSpread;
    }

    public double getPriceRange() {
        return priceRange;
    }

    public int getSize() {
        return size;
    }

    public double getCommission() {
        return commission;
    }
}
