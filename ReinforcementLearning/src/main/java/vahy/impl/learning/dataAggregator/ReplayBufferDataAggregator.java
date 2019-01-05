package vahy.impl.learning.dataAggregator;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferDataAggregator implements DataAggregator {

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer = new LinkedList<>();

    public ReplayBufferDataAggregator(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void addEpisodeSamples(List<ImmutableTuple<DoubleVector, double[]>> episodeHistory) {
        buffer.addLast(episodeHistory);
        while(buffer.size() > bufferSize) {
            buffer.removeFirst();
        }
    }

    @Override
    public ImmutableTuple<double[][], double[][]> getTrainingDataset() {
        List<ImmutableTuple<DoubleVector, double[]>> collect = buffer.stream().flatMap(Collection::stream).collect(Collectors.toList());
        double[][] input = new double[collect.size()][];
        double[][] output = new double[collect.size()][];
        int index = 0;
        for (ImmutableTuple<DoubleVector, double[]> immutableTuple : collect) {
            input[index] = immutableTuple.getFirst().getObservedVector();
            output[index] = immutableTuple.getSecond();
            index++;
        }
        return new ImmutableTuple<>(input, output);
    }
}
