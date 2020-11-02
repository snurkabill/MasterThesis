package vahy.impl.learning.dataAggregator;

import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;

import java.util.Map;

public class EveryVisitMonteCarloDataAggregator extends MonteCarloDataAggregator {

    public EveryVisitMonteCarloDataAggregator(Map<DoubleVector, MutableDoubleArray> visitAverageRewardMap) {
        super(visitAverageRewardMap);
    }

    @Override
    protected void putDataSample(Map<DoubleVector, MutableDoubleArray> everyVisitSet, MutableDoubleArray dataSample, DoubleVector observation) {
        if(!everyVisitSet.containsKey(observation)) {
            everyVisitSet.put(observation, dataSample);
        } else {
            everyVisitSet.get(observation).addDataSample(dataSample.getDoubleArray());
        }
    }

    @Override
    public boolean requiresStatesInOrder() {
        return false;
    }
}
