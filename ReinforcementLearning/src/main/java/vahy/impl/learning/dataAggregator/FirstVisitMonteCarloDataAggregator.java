package vahy.impl.learning.dataAggregator;

import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;

import java.util.Map;

public class FirstVisitMonteCarloDataAggregator extends MonteCarloDataAggregator {

    public FirstVisitMonteCarloDataAggregator(Map<DoubleVector, MutableDoubleArray> visitAverageRewardMap) {
        super(visitAverageRewardMap);
    }

    @Override
    protected void putDataSample(Map<DoubleVector, MutableDoubleArray> firstVisitSet, MutableDoubleArray dataSample, DoubleVector observation) {
        if(!firstVisitSet.containsKey(observation)) {
            firstVisitSet.put(observation, dataSample);
        }
    }
}
