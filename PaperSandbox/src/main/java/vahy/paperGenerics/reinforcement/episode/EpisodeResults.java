package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;

import java.util.List;

public class EpisodeResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>  {

    private final List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeHistory;
    private final int playerStepCount;
    private final int totalStepCount;
    private final double totalPayoff;
    private final boolean isRiskHit;
    private final long millisecondDuration;

    public EpisodeResults(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeHistory,
                          int playerStepCount, int totalStepCount, double totalPayoff, boolean isRiskHit, long millisecondDuration) {
        this.episodeHistory = episodeHistory;
        this.playerStepCount = playerStepCount;
        this.totalStepCount = totalStepCount;
        this.totalPayoff = totalPayoff;
        this.isRiskHit = isRiskHit;
        this.millisecondDuration = millisecondDuration;
    }

    public int getTotalStepCount() {
        return totalStepCount;
    }

    public int getPlayerStepCount() {
        return playerStepCount;
    }

    public double getTotalPayoff() {
        return totalPayoff;
    }

    public boolean isRiskHit() {
        return isRiskHit;
    }

    public long getMillisecondDuration() {
        return millisecondDuration;
    }

    public List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState>> getEpisodeHistory() {
        return episodeHistory;
    }

    public TState getFinalState() {
        return this.episodeHistory.get(episodeHistory.size() - 1).getToState();
    }
}
