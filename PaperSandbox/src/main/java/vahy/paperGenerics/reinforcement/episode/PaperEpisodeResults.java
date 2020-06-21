package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.episode.EpisodeResultsImpl;
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
    extends EpisodeResultsImpl<TAction, TObservation, TState, TPolicyRecord> {

    private final List<Boolean> isRiskHitList;

    public PaperEpisodeResults(List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> episodeHistory,
                               PolicyIdTranslationMap policyIdTranslationMap,
                               int policyCount,
                               List<Integer> playerStepCount,
                               int totalStepCount,
                               List<Double> totalPayoff,
                               Duration duration) {
        super(episodeHistory, policyIdTranslationMap, policyCount, playerStepCount, totalStepCount, totalPayoff, duration);
        isRiskHitList = new ArrayList<>(policyCount);
        for (int i = 0; i < policyCount; i++) {
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
    public String episodeMetadataToFile() {
        String super_ =  super.episodeMetadataToFile();
        var sb = new StringBuilder(super_);
        appendLine(sb, "Risk Hit", isRiskHitList.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
        return sb.toString();
    }
}
