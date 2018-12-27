package vahy.impl.learning.dataAggregator;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MonteCarloDataAggregator implements DataAggregator {

    public MonteCarloDataAggregator() {
    }

    private final Map<DoubleVector, MutableDoubleArray> visitAverageRewardMap = new LinkedHashMap<>();

    @Override
    public ImmutableTuple<double[][], double[][]> getTrainingDataset() {

        double[][] input = new double[visitAverageRewardMap.size()][];
        double[][] output = new double[visitAverageRewardMap.size()][];
        List<ImmutableTuple<double[], double[]>> collect = visitAverageRewardMap
            .entrySet()
            .stream()
            .map(x -> new ImmutableTuple<>(x.getKey().getObservedVector(), x.getValue().getDoubleArray()))
            .collect(Collectors.toList());
        int index = 0;
        for (ImmutableTuple<double[], double[]> immutableTuple : collect) {
            input[index] = immutableTuple.getFirst();
            output[index] = immutableTuple.getSecond();
            index++;
        }
        return new ImmutableTuple<>(input, output);
    }

    @Override
    public void addEpisodeSamples(List<ImmutableTuple<DoubleVector, double[]>> episodeHistory) {
        addVisitedRewards(calculatedVisitedRewards(episodeHistory));
    }

    protected abstract Map<DoubleVector, MutableDoubleArray> calculatedVisitedRewards(List<ImmutableTuple<DoubleVector, double[]>> episodeHistory);

    protected void addVisitedRewards(Map<DoubleVector, MutableDoubleArray> sampledStateVisitMap) {
        for (Map.Entry<DoubleVector, MutableDoubleArray> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableDoubleArray mutableDoubleArray = visitAverageRewardMap.get(entry.getKey());
                mutableDoubleArray.addDataSample(entry.getValue().getDoubleArray());
            } else {
                visitAverageRewardMap.put(entry.getKey(), new MutableDoubleArray(entry.getValue().getDoubleArray(), false));
            }
        }
    }
}
