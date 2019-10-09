package vahy.impl.learning.dataAggregator;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferDataAggregator implements DataAggregator {

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, MutableDoubleArray>>> buffer;

    public ReplayBufferDataAggregator(int bufferSize, LinkedList<List<ImmutableTuple<DoubleVector, MutableDoubleArray>>> buffer) {
        this.bufferSize = bufferSize;
        this.buffer = buffer;
    }

    @Override
    public void addEpisodeSamples(List<ImmutableTuple<DoubleVector, MutableDoubleArray>> episodeHistory) {
        buffer.addLast(episodeHistory);
        while(buffer.size() > bufferSize) {
            buffer.removeFirst();
        }
    }

    @Override
    public ImmutableTuple<DoubleVector[], double[][]> getTrainingDataset() {
        List<ImmutableTuple<DoubleVector, MutableDoubleArray>> collect = buffer
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        DoubleVector[] input = new DoubleVector[collect.size()];
        double[][] output = new double[collect.size()][];
        int index = 0;
        for (ImmutableTuple<DoubleVector, MutableDoubleArray> immutableTuple : collect) {
            input[index] = immutableTuple.getFirst();
            output[index] = immutableTuple.getSecond().getDoubleArray();
            index++;
        }
        return new ImmutableTuple<>(input, output);
    }
}
