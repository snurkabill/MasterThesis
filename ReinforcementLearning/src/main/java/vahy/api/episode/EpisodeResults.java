package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.time.Duration;
import java.util.List;

public interface EpisodeResults<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>> {

    List<EpisodeStepRecord<TAction, TObservation, TState>> getEpisodeHistory();

    int getPolicyCount();

    int getTotalStepCount();

    PolicyIdTranslationMap getPolicyIdTranslationMap();

    List<Integer> getPlayerStepCountList();

    List<Double> getAverageDurationPerDecision();

    List<Double> getTotalPayoff();

    Duration getDuration();

    TState getFinalState();

    String episodeMetadataToFile();

}
