package vahy.impl.learning.trainer.earlyStoppingStrategies;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EarlyStoppingStrategy;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class AlwaysFalseStoppingStrategy<
        TAction extends Enum<TAction> & Action,
        TObservation extends Observation<TObservation>,
        TState extends State<TAction, TObservation, TState>,
        TStatistics extends EpisodeStatistics> implements EarlyStoppingStrategy<TAction, TObservation, TState, TStatistics> {
    @Override
    public boolean isStoppingConditionFulfilled(ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> trainingEpisodes) {
        return false;
    }

    @Override
    public boolean isStoppingConditionFulfilled(ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> trainingEpisodes,
                                                ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> evaluationEpisodes) {
        return false;
    }
}
