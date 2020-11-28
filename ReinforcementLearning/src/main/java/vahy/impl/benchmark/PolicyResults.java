package vahy.impl.benchmark;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.List;

public class PolicyResults<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>,
    TStatistics extends EpisodeStatistics> {

    private final String runName;
    private final List<OptimizedPolicy<TAction, TObservation, TState>> policyList;
    private final List<TStatistics> trainingStatisticsList;
    private final TStatistics evaluationStatistics;
    private final Duration trainingDuration;
    private final Duration benchmarkingDuration;

    public PolicyResults(String runName,
                         List<OptimizedPolicy<TAction, TObservation, TState>> policyList,
                         List<TStatistics> trainingStatisticsList,
                         TStatistics evaluationStatistics,
                         Duration trainingDuration,
                         Duration benchmarkingDuration) {
        this.runName = runName;
        this.policyList = policyList;
        this.trainingStatisticsList = trainingStatisticsList;
        this.evaluationStatistics = evaluationStatistics;
        this.trainingDuration = trainingDuration;
        this.benchmarkingDuration = benchmarkingDuration;
    }

    public String getRunName() {
        return runName;
    }

    public List<OptimizedPolicy<TAction, TObservation, TState>> getPolicyList() {
        return policyList;
    }

    public List<TStatistics> getTrainingStatisticsList() {
        return trainingStatisticsList;
    }

    public Duration getTrainingDuration() {
        return trainingDuration;
    }

    public Duration getBenchmarkingDuration() {
        return benchmarkingDuration;
    }

    public TStatistics getEvaluationStatistics() {
        return evaluationStatistics;
    }

}