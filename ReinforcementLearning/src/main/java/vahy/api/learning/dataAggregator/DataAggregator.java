package vahy.api.learning.dataAggregator;

import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface DataAggregator {

    void addEpisodeSamples(List<ImmutableTuple<DoubleVector, MutableDoubleArray>>  episodeHistory);

    ImmutableTuple<DoubleVector[], double[][]> getTrainingDataset();

    // TODO: add printing and dumping dataset

}
