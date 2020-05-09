package vahy.api.experiment;

import java.util.function.Supplier;

public interface PolicyConfig extends Config {

    double getDiscountFactor();

    Supplier<Double> getExplorationConstantSupplier();

    Supplier<Double> getTemperatureSupplier();

}
