package vahy.paperGenerics.experiment;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.utils.ImmutableTuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaperPolicyResults<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> benchmarkingPolicy;
    private final List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeList;
    private final CalculatedResultStatistics calculatedResultStatistics;
    private final long benchmarkingMilliseconds;

    public PaperPolicyResults(PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> benchmarkingPolicy,
                              List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeList,
                              CalculatedResultStatistics calculatedResultStatistics,
                              long benchmarkingMilliseconds) {
        this.benchmarkingPolicy = benchmarkingPolicy;
        this.episodeList = episodeList;
        this.calculatedResultStatistics = calculatedResultStatistics;
        this.benchmarkingMilliseconds = benchmarkingMilliseconds;
    }

    public PaperBenchmarkingPolicy<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> getBenchmarkingPolicy() {
        return benchmarkingPolicy;
    }

    public List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState>> getEpisodeList() {
        return episodeList;
    }

    public long getBenchmarkingMilliseconds() {
        return benchmarkingMilliseconds;
    }

    public CalculatedResultStatistics getCalculatedResultStatistics() {
        return calculatedResultStatistics;
    }

    public List<ImmutableTuple<Double, Boolean>> getRewardAndRiskList() {

        double[] totalRewardList = this.episodeList.stream()
            .mapToDouble(x -> x.getEpisodeStateRewardReturnList().stream().mapToDouble(y -> y.getReward()).sum())
            .toArray();
        var totalRiskList = this.episodeList.stream()
            .map(EpisodeResults::isRiskHit)
            .toArray();
        var returnList = new ArrayList<ImmutableTuple<Double, Boolean>>(totalRewardList.length);
        for (int i = 0; i < totalRewardList.length; i++) {
            returnList.add(new ImmutableTuple<>(totalRewardList[i], (Boolean) totalRiskList[i]));
        }
        return returnList;
    }

    public void dumpResultsToFile(String path) throws IOException {
        File rewardsAndRisksFile = new File(path, "RewardsAndRisks");
        writeEpisodeResultsToFile(rewardsAndRisksFile.getAbsolutePath(), this.getRewardAndRiskList());

        File statisticsFile = new File(path, "Statistics");
        writeStatisticsToFile(statisticsFile.getAbsolutePath(), calculatedResultStatistics);
    }

    private void writeEpisodeResultsToFile(String filename, List<ImmutableTuple<Double, Boolean>> list) throws IOException{
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        outputWriter.write("Reward,Risk");
        outputWriter.newLine();
        for (int i = 0; i < list.size(); i++) {
            outputWriter.write(list.get(i).getFirst() + "," + list.get(i).getSecond());
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    private void writeStatisticsToFile(String filename, CalculatedResultStatistics calculatedResultStatistics) throws IOException {
        BufferedWriter outputWritter = new BufferedWriter(new FileWriter(filename));
        outputWritter.write(calculatedResultStatistics.printToFile());
        outputWritter.flush();
        outputWritter.close();
    }

}
