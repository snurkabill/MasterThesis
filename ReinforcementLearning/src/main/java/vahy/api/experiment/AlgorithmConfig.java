package vahy.api.experiment;

import java.util.function.Supplier;

public interface AlgorithmConfig extends Config {

    String getAlgorithmId();

    double getDiscountFactor();

    int getBatchEpisodeCount();

    int getStageCount();

    Supplier<Double> getExplorationConstantSupplier();

    Supplier<Double> getTemperatureSupplier();

}
