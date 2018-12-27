package vahy.api.learning.dataAggregator;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface DataAggregator {

    void addEpisodeSamples(List<ImmutableTuple<DoubleVector, double[]>> episodeHistory);

    ImmutableTuple<double[][], double[][]> getTrainingDataset();

}
