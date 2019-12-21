package vahy.api.learning.dataAggregator;

import vahy.api.experiment.AlgorithmConfig;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.utils.EnumUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public enum DataAggregationAlgorithm {

    FIRST_VISIT_MC,
    EVERY_VISIT_MC,
    REPLAY_BUFFER;

    public DataAggregator resolveDataAggregator(AlgorithmConfig algorithmConfig) {
        switch(this) {
            case REPLAY_BUFFER:
                return new ReplayBufferDataAggregator(algorithmConfig.getReplayBufferSize(), new LinkedList<>());
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(this);
        }
    }

}
