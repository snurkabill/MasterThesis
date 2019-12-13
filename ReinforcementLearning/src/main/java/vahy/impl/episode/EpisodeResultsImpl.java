package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.List;

public class EpisodeResultsImpl<
    TAction extends Enum<TAction> & Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private final List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory;
    private final int playerStepCount;
    private final int totalStepCount;
    private final double totalPayoff;
    private final Duration duration;

    public EpisodeResultsImpl(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistory,
                              int playerStepCount,
                              int totalStepCount,
                              double totalPayoff,
                              Duration duration) {
        this.episodeHistory = episodeHistory;
        this.playerStepCount = playerStepCount;
        this.totalStepCount = totalStepCount;
        this.totalPayoff = totalPayoff;
        this.duration = duration;
    }


    @Override
    public List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> getEpisodeHistory() {
        return episodeHistory;
    }

    @Override
    public int getTotalStepCount() {
        return totalStepCount;
    }

    @Override
    public int getPlayerStepCount() {
        return playerStepCount;
    }

    @Override
    public double getTotalPayoff() {
        return totalPayoff;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public TState getFinalState() {
        return episodeHistory.get(episodeHistory.size() - 1).getToState();
    }

    protected void appendLine(StringBuilder sb, String propertyName, String propertyValue) {
        sb.append(propertyName);
        sb.append(", ");
        sb.append(propertyValue);
        sb.append(System.lineSeparator());
    }

    @Override
    public String episodeMetadataToFile() {
        var sb = new StringBuilder();
        appendLine(sb, "Total step count", String.valueOf(getTotalStepCount()));
        appendLine(sb, "Player step count", String.valueOf(getPlayerStepCount()));
        appendLine(sb, "Duration [ms]", String.valueOf(getDuration().toMillis()));
        appendLine(sb, "Total Payoff", String.valueOf(getTotalPayoff()));
        return sb.toString();
    }
}
