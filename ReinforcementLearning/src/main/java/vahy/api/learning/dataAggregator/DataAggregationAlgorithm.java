package vahy.api.learning.dataAggregator;

import vahy.api.experiment.ApproximatorConfig;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.utils.EnumUtils;

import java.util.LinkedHashMap;

public enum DataAggregationAlgorithm {

    FIRST_VISIT_MC,
    EVERY_VISIT_MC,
    REPLAY_BUFFER;

    public DataAggregator resolveDataAggregator(ApproximatorConfig algorithmConfig) {
        switch(this) {
            case REPLAY_BUFFER:
                return new ReplayBufferDataAggregator(algorithmConfig.getReplayBufferSize());
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(this);
        }
    }

}
