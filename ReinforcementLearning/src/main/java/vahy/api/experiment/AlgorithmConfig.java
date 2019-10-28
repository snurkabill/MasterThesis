package vahy.api.experiment;

import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;

public interface AlgorithmConfig extends Config {

    double getDiscountFactor();

    int getBatchEpisodeCount();

    int getReplayBufferSize();

    int getMaximalStepCountBound();

    int getStageCount();

    ParameterSupplier<Double> getExplorationConstantSupplier();

    ParameterSupplier<Double> getTemperatureSupplier();

    DataAggregationAlgorithm getDataAggregationAlgorithm();

    ApproximatorType getApproximatorType();

}
