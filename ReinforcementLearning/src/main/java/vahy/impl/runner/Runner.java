package vahy.impl.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.impl.benchmark.OptimizedPolicy;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.learning.trainer.Trainer;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class Runner<TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    public PolicyResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> run(
        RunnerArguments<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> runnerArguments,
        EvaluationArguments<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> evaluationArguments) throws IOException
    {
        var trainingStatistics = optimizePolicy(runnerArguments);
        var optimizedPolicy = new OptimizedPolicy<>(
            runnerArguments.getPolicyId(),
            runnerArguments.getTrainablePredictor(),
            runnerArguments.getPolicySupplier());
        var evaluationResults = evaluatePolicy(optimizedPolicy, evaluationArguments);

        return new PolicyResults<>(optimizedPolicy, trainingStatistics.getSecond(), evaluationResults.getFirst(), trainingStatistics.getFirst(), evaluationResults.getSecond());
    }

    private void closeResources(OptimizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy) throws IOException {
        policy.getTrainablePredictor().close();
        logger.debug("Resources of trainable policy [{}] closed. ", policy.getPolicyId());
    }

    private ImmutableTuple<Duration, List<TStatistics>> optimizePolicy(RunnerArguments<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> runnerArguments) {
        var progressTrackerSettings = new ProgressTrackerSettings(true, runnerArguments.getSystemConfig().isDrawWindow(), false, false);
        var gameSampler = new GameSamplerImpl<>(
            runnerArguments.getInitialStateSupplier(),
            runnerArguments.getEpisodeResultsFactory(),
            PolicyMode.TRAINING,
            runnerArguments.getSystemConfig().getParallelThreadsCount(),
            runnerArguments.getPolicySupplier(),
            runnerArguments.getOpponentPolicySupplier());

        var trainer = new Trainer<>(
            runnerArguments.getTrainablePredictor(),
            gameSampler,
            runnerArguments.getDataAggregator(),
            runnerArguments.getDataMaker(),
            progressTrackerSettings,
            runnerArguments.getProblemConfig(),
            runnerArguments.getEpisodeStatisticsCalculator(),
            runnerArguments.getAdditionalDataPointGeneratorList());

        logger.info("Training policy [{}]", runnerArguments.getPolicyId());

        var trainingCycle = new PolicyTrainingCycle<>(
            runnerArguments.getSystemConfig(),
            runnerArguments.getAlgorithmConfig(),
            runnerArguments.getEpisodeWriter(),
            trainer);

        var durationWithStatistics = trainingCycle.startTraining();
        logger.info("Training of [{}] policy took: [{}] ms", runnerArguments.getPolicyId(), durationWithStatistics.getFirst().toMillis());
        return durationWithStatistics;
    }

    public ImmutableTuple<TStatistics, Duration> evaluatePolicy(
        OptimizedPolicy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> policy,
        EvaluationArguments<TConfig, TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord, TStatistics> evaluationArguments)
    {
        var systemConfig = evaluationArguments.getSystemConfig();
        logger.info("Running evaluation inference of [{}] policy for [{}] iterations", policy.getPolicyId(), systemConfig.getEvalEpisodeCount());
        var gameSampler = new GameSamplerImpl<>(
            evaluationArguments.getInitialStateSupplier(),
            evaluationArguments.getEpisodeResultsFactory(),
            PolicyMode.INFERENCE,
            systemConfig.isSingleThreadedEvaluation() ? 1 : systemConfig.getParallelThreadsCount(),
            policy.getPolicySupplier(),
            evaluationArguments.getOpponentPolicySupplier());
        long start = System.currentTimeMillis();
        var episodeList = gameSampler.sampleEpisodes(systemConfig.getEvalEpisodeCount(), evaluationArguments.getProblemConfig().getMaximalStepCountBound());
        long end = System.currentTimeMillis();
        logger.info("Evaluation of [{}] policy in [{}] runs took [{}] milliseconds", policy.getPolicyId(), systemConfig.getEvalEpisodeCount(), end - start);
        var duration = Duration.ofMillis(end - start);

        if(systemConfig.dumpEvaluationData()) {
            evaluationArguments.getEpisodeWriter().writeEvaluationEpisode(episodeList);
        }
        return new ImmutableTuple<>(evaluationArguments.getEpisodeStatisticsCalculator().calculateStatistics(episodeList, duration), duration);
    }
}
