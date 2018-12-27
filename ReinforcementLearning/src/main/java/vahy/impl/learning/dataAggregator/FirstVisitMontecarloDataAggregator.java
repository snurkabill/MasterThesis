package vahy.impl.learning.dataAggregator;

import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirstVisitMontecarloDataAggregator extends MonteCarloDataAggregator {

    @Override
    protected Map<DoubleVector, MutableDoubleArray> calculatedVisitedRewards(List<ImmutableTuple<DoubleVector, double[]>> episodeHistory) {
        Map<DoubleVector, MutableDoubleArray> everyVisitSet = new LinkedHashMap<>();
        for (ImmutableTuple<DoubleVector, double[]> entry : episodeHistory) {
            DoubleVector observation = entry.getFirst();
            if (!everyVisitSet.containsKey(observation)) {
                everyVisitSet.put(observation, new MutableDoubleArray(entry.getSecond(), false));
            }
        }
        return everyVisitSet;
    }
}
