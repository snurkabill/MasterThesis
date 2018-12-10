package vahy.paperGenerics.benchmark;

import vahy.api.model.Action;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;

import java.util.List;

public class PaperPolicyResults<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>> {

    private final PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState> benchmarkingPolicy;
    private final List<EpisodeResults<TAction, TReward, TObservation, TState>> episodeList;
    private final double averageNanosPerEpisode;
    private final double averageMillisPerEpisode;
    private final double averageReward;
    private final long riskHitCounter;
    private final double riskHitRatio;

    public PaperPolicyResults(PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState> benchmarkingPolicy,
                              List<EpisodeResults<TAction, TReward, TObservation, TState>> episodeList,
                              double averageNanosPerEpisode) {
        this.benchmarkingPolicy = benchmarkingPolicy;
        this.episodeList = episodeList;
        this.averageNanosPerEpisode = averageNanosPerEpisode;
        this.riskHitCounter = episodeList.stream().filter(EpisodeResults::isRiskHit).count();
        this.riskHitRatio = riskHitCounter / (double) episodeList.size();
        this.averageReward = episodeList
            .stream()
            .mapToDouble(x -> x
                .getEpisodeStateRewardReturnList()
                .stream()
                .mapToDouble(y -> y.getReward().getValue()).sum())
            .sum() / (double) episodeList.size();
        this.averageMillisPerEpisode = episodeList.stream().mapToDouble(EpisodeResults::getMillisecondDuration).sum() / (double) episodeList.size();
    }

    public PaperBenchmarkingPolicy<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getBenchmarkingPolicy() {
        return benchmarkingPolicy;
    }

    public List<EpisodeResults<TAction, TReward, TObservation, TState>> getEpisodeList() {
        return episodeList;
    }

    public double getAverageNanosPerEpisode() {
        return averageNanosPerEpisode;
    }

    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
    }

    public double getAverageReward() {
        return averageReward;
    }

    public long getRiskHitCounter() {
        return riskHitCounter;
    }

    public double getRiskHitRatio() {
        return riskHitRatio;
    }
}
