package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaperEpisodeResults<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends PaperState<TAction, TObservation, TState>,
    TPolicyRecord extends PaperPolicyRecord>
    implements EpisodeResults<TAction, TObservation, TState, TPolicyRecord> {

    private final EpisodeResults<TAction, TObservation, TState, TPolicyRecord> base;
    private final List<Boolean> isRiskHitList;

    public PaperEpisodeResults(EpisodeResults<TAction, TObservation, TState, TPolicyRecord> base) {
        this.base = base;
        isRiskHitList = new ArrayList<>(base.getPolicyCount());
        for (int i = 0; i < base.getPolicyCount(); i++) {
            isRiskHitList.add(getFinalState().isRiskHit(i));
        }
    }

    public List<Boolean> isRiskHit() {
        return isRiskHitList;
    }

    public boolean isRiskHit(int playerId) {
        return isRiskHitList.get(playerId);
    }

    @Override
    public List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> getEpisodeHistory() {
        return base.getEpisodeHistory();
    }

    @Override
    public int getPolicyCount() {
        return base.getPolicyCount();
    }

    @Override
    public int getTotalStepCount() {
        return base.getTotalStepCount();
    }

    @Override
    public PolicyIdTranslationMap getPolicyIdTranslationMap() {
        return base.getPolicyIdTranslationMap();
    }

    @Override
    public List<Integer> getPlayerStepCountList() {
        return base.getPlayerStepCountList();
    }

    @Override
    public List<Double> getAverageDurationPerDecision() {
        return base.getAverageDurationPerDecision();
    }

    @Override
    public List<Double> getTotalPayoff() {
        return base.getTotalPayoff();
    }

    @Override
    public Duration getDuration() {
        return base.getDuration();
    }

    @Override
    public TState getFinalState() {
        return base.getFinalState();
    }

    @Override
    public String episodeMetadataToFile() {
        String super_ =  base.episodeMetadataToFile();
        var sb = new StringBuilder(super_);
        sb.append("Risk Hit");
        sb.append(", ");
        sb.append(isRiskHitList.stream().map(Object::toString).collect(Collectors.joining(", ")));
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
