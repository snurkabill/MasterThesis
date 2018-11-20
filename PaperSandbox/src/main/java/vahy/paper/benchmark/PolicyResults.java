package vahy.paper.benchmark;

import vahy.paper.reinforcement.episode.PaperEpisode;

import java.util.List;

public class PolicyResults {

    private final BenchmarkingPolicy benchmarkingPolicy;
    private final List<PaperEpisode> episodeList;
    private final double averageNanosPerEpisode;
    private final double averageMillisPerEpisode;
    private final double averageReward;
    private final long agentKillCounter;
    private final double killRatio;

    public PolicyResults(BenchmarkingPolicy benchmarkingPolicy, List<PaperEpisode> episodeList, Double averageNanosPerEpisode) {
        this.benchmarkingPolicy = benchmarkingPolicy;
        this.episodeList = episodeList;
        this.averageNanosPerEpisode = averageNanosPerEpisode;
        this.agentKillCounter = episodeList.stream().filter(PaperEpisode::isAgentKilled).count();
        this.killRatio = agentKillCounter / (double) episodeList.size();
        this.averageReward = episodeList
            .stream()
            .mapToDouble(x -> x
                .getEpisodeStateRewardReturnList()
                .stream()
                .mapToDouble(y -> y.getReward().getValue()).sum())
            .sum() / (double) episodeList.size();
        this.averageMillisPerEpisode = episodeList.stream().mapToDouble(PaperEpisode::getMillisecondDuration).sum() / (double) episodeList.size();
    }

    public BenchmarkingPolicy getBenchmarkingPolicy() {
        return benchmarkingPolicy;
    }

    public List<PaperEpisode> getEpisodeList() {
        return episodeList;
    }

    public double getAverageNanosPerEpisode() {
        return averageNanosPerEpisode;
    }

    public double getAverageReward() {
        return averageReward;
    }

    public long getAgentKillCounter() {
        return agentKillCounter;
    }

    public double getKillRatio() {
        return killRatio;
    }

    public double getAverageMillisPerEpisode() {
        return averageMillisPerEpisode;
    }
}
