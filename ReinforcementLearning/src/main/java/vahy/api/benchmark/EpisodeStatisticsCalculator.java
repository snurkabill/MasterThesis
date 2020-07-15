package vahy.api.benchmark;

import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.time.Duration;
import java.util.List;

public interface EpisodeStatisticsCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TStatistics extends EpisodeStatistics> {

    TStatistics calculateStatistics(List<EpisodeResults<TAction, TObservation, TState>> episodeResultsList, Duration duration);
}
