package vahy.environment;

import vahy.environment.lookback.LookbackWrapperImpl;

import java.util.List;

public class StaticMarketPart<T> {

    private final LookbackWrapperImpl<T> lookbackWrapper;

    public StaticMarketPart(LookbackWrapperImpl<T> lookbackWrapper) {
        this.lookbackWrapper = lookbackWrapper;
    }

    public List<Double> getLookback() {
        return lookbackWrapper.getLookback();
    }
}
