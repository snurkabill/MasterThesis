package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.MyShittyFrameVisualization;
import vahy.vizualiation.XYDatasetBuilder;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Benchmark<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private final List<ImmutableTuple<Function<TStatistics, Double>, String>> mapperList;
    private final SystemConfig systemConfig;

    public Benchmark(List<ImmutableTuple<Function<TStatistics, Double>, String>> mapperList, SystemConfig systemConfig) {
        this.mapperList = mapperList;
        this.systemConfig = systemConfig;
    }

    public void benchmark(List<PolicyResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics>> policyList) {
        createReport(
            policyList.stream().sorted(Comparator.comparing(x -> x.getPolicy().getPolicyId())).map(PolicyResults::getTrainingStatisticsList).collect(Collectors.toList()),
            policyList.stream().sorted(Comparator.comparing(x -> x.getPolicy().getPolicyId())).map(PolicyResults::getEpisodeStatistics).collect(Collectors.toList()),
            policyList.stream().sorted(Comparator.comparing(x -> x.getPolicy().getPolicyId())).map(x -> x.getPolicy().getPolicyId()).collect(Collectors.toList()),
            mapperList,
            systemConfig);
    }

    private void createReport(List<List<TStatistics>> trainingStatisticsList,
                              List<TStatistics> evaluationStatisticsList,
                              List<String> policyIdList,
                              List<ImmutableTuple<Function<TStatistics, Double>, String>> mapperList,
                              SystemConfig systemConfig)
    {
        List<ImmutableTuple<List<List<Double>>, String>> collectedTrainingValues = mapperList
            .stream()
            .map(x -> new ImmutableTuple<>(
                extractElement(trainingStatisticsList, x.getFirst()),
                x.getSecond()))
            .collect(Collectors.toList());

        if(systemConfig.isDrawWindow()) {
            MyShittyFrameVisualization myShittyFrameVisualization = new MyShittyFrameVisualization(
                "Benchmark results",
                collectedTrainingValues.stream().map(ImmutableTuple::getSecond).sorted(String::compareTo).collect(Collectors.toList()),
                "Iteration",
                "Value",
                Color.RED);

            var datasetList = collectedTrainingValues
                .stream()
                .sorted(Comparator.comparing(ImmutableTuple::getSecond))
                .map(x -> XYDatasetBuilder.createDatasetWithFixedX(x.getFirst(), policyIdList))
                .collect(Collectors.toList());
            myShittyFrameVisualization.draw(datasetList);
        }
    }

    private List<List<Double>> extractElement(List<List<TStatistics>> stats, Function<TStatistics, Double> mapper) {
        return stats.stream().map(x -> x.stream().map(mapper).collect(Collectors.toList())).collect(Collectors.toList());
    }

}
