package vahy.api.experiment;

import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;

import java.util.function.Supplier;

public interface AlgorithmConfig extends Config {

    double getDiscountFactor();

    int getBatchEpisodeCount();

    int getReplayBufferSize();

    int getStageCount();

    Supplier<Double> getExplorationConstantSupplier();

    Supplier<Double> getTemperatureSupplier();

    DataAggregationAlgorithm getDataAggregationAlgorithm();

    ApproximatorType getApproximatorType();

}
