package vahy.api.learning.trainer;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.impl.model.observation.DoubleVector;

public class EpisodeStepRecordWithObservation<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> {

    private final EpisodeStepRecord<TAction, DoubleVector, TState> episodeStepRecord;
    private final DoubleVector observation;

    public EpisodeStepRecordWithObservation(EpisodeStepRecord<TAction, DoubleVector, TState> episodeStepRecord, DoubleVector observation) {
        this.episodeStepRecord = episodeStepRecord;
        this.observation = observation;
    }

    public EpisodeStepRecord<TAction, DoubleVector, TState> getEpisodeStepRecord() {
        return episodeStepRecord;
    }

    public DoubleVector getObservation() {
        return observation;
    }
}
