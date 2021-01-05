package vahy.api.learning.trainer;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface EarlyStoppingStrategy<
        TAction extends Enum<TAction> & Action,
        TObservation extends Observation<TObservation>,
        TState extends State<TAction, TObservation, TState>,
        TStatistics extends EpisodeStatistics> {
    boolean isStoppingConditionFulfilled(ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> trainingEpisodes);
    boolean isStoppingConditionFulfilled(ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> trainingEpisodes,
                                         ImmutableTuple<List<EpisodeResults<TAction, TObservation, TState>>, TStatistics> evaluationEpisodes);
}
