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

    private final Map<DoubleVector, MutableDoubleArray> visitAverageRewardMap;

    public MonteCarloDataAggregator(Map<DoubleVector, MutableDoubleArray> visitAverageRewardMap) {
        this.visitAverageRewardMap = visitAverageRewardMap;
    }

    @Override
    public ImmutableTuple<DoubleVector[], double[][]> getTrainingDataset() {
        DoubleVector[] input = new DoubleVector[visitAverageRewardMap.size()];
        double[][] output = new double[visitAverageRewardMap.size()][];
        List<ImmutableTuple<DoubleVector, double[]>> collect = visitAverageRewardMap
            .entrySet()
            .stream()
            .map(x -> new ImmutableTuple<>(x.getKey(), x.getValue().getDoubleArray()))
            .collect(Collectors.toList());
        int index = 0;
        for (ImmutableTuple<DoubleVector, double[]> immutableTuple : collect) {
            input[index] = immutableTuple.getFirst();
            output[index] = immutableTuple.getSecond();
            index++;
        }
        return new ImmutableTuple<>(input, output);
    }

    @Override
    public void addEpisodeSamples(List<ImmutableTuple<DoubleVector, MutableDoubleArray>> episodeHistory) {
        addVisitedRewards(calculatedVisitedRewards(episodeHistory));
    }

    protected abstract void putDataSample(Map<DoubleVector, MutableDoubleArray> firstVisitSet, MutableDoubleArray dataSample, DoubleVector observation);

    protected Map<DoubleVector, MutableDoubleArray> calculatedVisitedRewards(List<ImmutableTuple<DoubleVector, MutableDoubleArray>> episodeHistory) {
        Map<DoubleVector, MutableDoubleArray> visitSet = new LinkedHashMap<>(episodeHistory.size());
        for (int i = 0; i < episodeHistory.size(); i++) {
            var dataSample = episodeHistory.get(i);
            putDataSample(visitSet, dataSample.getSecond(), dataSample.getFirst());
        }
        return visitSet;
    }

    protected void addVisitedRewards(Map<DoubleVector, MutableDoubleArray> sampledStateVisitMap) {
        for (Map.Entry<DoubleVector, MutableDoubleArray> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableDoubleArray mutableDoubleArray = visitAverageRewardMap.get(entry.getKey());
                mutableDoubleArray.addDataSample(entry.getValue().getDoubleArray(), entry.getValue().getCounter());
            } else {
                visitAverageRewardMap.put(entry.getKey(), new MutableDoubleArray(entry.getValue().getDoubleArray(), false));
            }
        }
    }
}
