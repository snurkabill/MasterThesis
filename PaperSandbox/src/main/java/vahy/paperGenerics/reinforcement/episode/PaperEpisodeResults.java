package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.episode.EpisodeResultsImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.time.Duration;
import java.util.List;

public class PaperEpisodeResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord>
    extends EpisodeResultsImpl<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    public PaperEpisodeResults(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory,
                               int playerStepCount,
                               int totalStepCount,
                               double totalPayoff,
                               Duration duration) {
        super(episodeHistory, playerStepCount, totalStepCount, totalPayoff, duration);
    }

    public boolean isRiskHit() {
        return getFinalState().isRiskHit();
    }

    @Override
    public String episodeMetadataToFile() {
        String super_ =  super.episodeMetadataToFile();
        var sb = new StringBuilder(super_);
        appendLine(sb, "Risk Hit", String.valueOf(getFinalState().isRiskHit()));
        return sb.toString();
    }
}
