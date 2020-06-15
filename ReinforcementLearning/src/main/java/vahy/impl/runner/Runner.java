package vahy.impl.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.PolicyCategory;
import vahy.api.experiment.ProblemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.benchmark.OptimizedPolicy;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.Trainer;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Runner<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord,
    TStatistics extends EpisodeStatistics> {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    private ImmutableTuple<Duration, List<TStatistics>> optimizePolicies(RunnerArguments<TConfig, TAction, TObservation, TState, TPolicyRecord, TStatistics> runnerArguments) {
        if(runnerArguments.getPolicyDefinitionList().stream().mapToLong(x -> x.getTrainablePredictorSetupList().size()).sum() > 0) {
            return optimizePolicies_inner(runnerArguments);
        } else {
            logger.info("There is no trainable predictors within policies. Skipping training.");
            return new ImmutableTuple<>(Duration.ZERO, new ArrayList<>(0));
        }
    }

    public PolicyResults<TAction, TObservation, TState, TPolicyRecord, TStatistics> run(RunnerArguments<TConfig, TAction, TObservation, TState, TPolicyRecord, TStatistics> runnerArguments,
                                                                                        EvaluationArguments<TConfig, TAction, TObservation, TState, TPolicyRecord, TStatistics> evaluationArguments) throws IOException
    {
        var trainingStatistics = optimizePolicies(runnerArguments);
        var optimizedPolicyList = runnerArguments.getPolicyDefinitionList().stream().map(x ->
            new OptimizedPolicy<>(
                x.getPolicyId(),
                x.getCategoryId(),
                x.getTrainablePredictorSetupList(),
                x.getPolicySupplierFactory()
            )).collect(Collectors.toList());
        var evaluationResults = evaluatePolicies(optimizedPolicyList, evaluationArguments);
        closeResources(runnerArguments.getPolicyDefinitionList().stream().flatMap(x -> x.getTrainablePredictorSetupList().stream()).collect(Collectors.toList()));
        return new PolicyResults<>(runnerArguments.getRunName(), optimizedPolicyList, trainingStatistics.getSecond(), evaluationResults.getFirst(), trainingStatistics.getFirst(), evaluationResults.getSecond());
    }

    private void closeResources(List<PredictorTrainingSetup<TAction, TObservation, TState, TPolicyRecord>> predictorList) throws IOException {
        var referenceSet = predictorList.stream().map(PredictorTrainingSetup::getTrainablePredictor).collect(Collectors.toSet());
        for (TrainablePredictor trainablePredictor : referenceSet) {
            trainablePredictor.close();
        }
        logger.debug("Resources of trainable predictors closed.");
    }

    private ImmutableTuple<Duration, List<TStatistics>> optimizePolicies_inner(RunnerArguments<TConfig, TAction, TObservation, TState, TPolicyRecord, TStatistics> runnerArguments) {
        var progressTrackerSettings = new ProgressTrackerSettings(true, runnerArguments.getSystemConfig().isDrawWindow(), false, false);
        var random = runnerArguments.getFinalMasterRandom();

        List<PolicyCategory<TAction, TObservation, TState, TPolicyRecord>> policyCategories = runnerArguments.getPolicyDefinitionList()
            .stream()
            .map(x -> x.getPolicySupplierFactory().createPolicySupplier(x.getPolicyId(), x.getCategoryId(), random.split()))
            .collect(Collectors.groupingBy(
                PolicySupplier::getPolicyCategoryId,
                Collectors.mapping(Function.identity(), Collectors.toList())))
            .entrySet()
            .stream()
            .map(x -> new PolicyCategory<>(x.getKey(), x.getValue()))
            .collect(Collectors.toList());

        var gameSampler = new GameSamplerImpl<>(
            runnerArguments.getInitialStateSupplier(),
            runnerArguments.getStateStateWrapperInitializer(),
            runnerArguments.getEpisodeResultsFactory(),
            policyCategories,
            runnerArguments.getProblemConfig().getPolicyShuffleStrategy(),
            runnerArguments.getSystemConfig().getParallelThreadsCount(),
            runnerArguments.getProblemConfig().getPolicyCategoryInfoList(),
            random.split());

        var trainer = new Trainer<>(
            gameSampler,
            runnerArguments.getPolicyDefinitionList().stream().flatMap(x -> x.getTrainablePredictorSetupList().stream()).distinct().collect(Collectors.toList()),
            progressTrackerSettings,
            runnerArguments.getProblemConfig(),
            runnerArguments.getEpisodeStatisticsCalculator(),
            runnerArguments.getAdditionalDataPointGeneratorList());

        logger.info("Training policy [{}]", runnerArguments.getRunName());

        var trainingCycle = new PolicyTrainingCycle<>(runnerArguments.getSystemConfig(), runnerArguments.getAlgorithmConfig(), runnerArguments.getEpisodeWriter(), trainer);

        var durationWithStatistics = trainingCycle.startTraining();
        logger.info("Training of [{}] took: [{}] ms", runnerArguments.getRunName(), durationWithStatistics.getFirst().toMillis());
        return durationWithStatistics;
    }

    public ImmutableTuple<TStatistics, Duration> evaluatePolicies(List<OptimizedPolicy<TAction, TObservation, TState, TPolicyRecord>> policyList,
                                                                  EvaluationArguments<TConfig, TAction, TObservation, TState, TPolicyRecord, TStatistics> evaluationArguments)
    {
        var systemConfig = evaluationArguments.getSystemConfig();
        var random = evaluationArguments.getFinalMasterRandom();
        logger.info("Running evaluation inference of [{}] policy for [{}] iterations", evaluationArguments.getRunName(), systemConfig.getEvalEpisodeCount());

        List<PolicyCategory<TAction, TObservation, TState, TPolicyRecord>> policyCategories = policyList
            .stream()
            .map(x -> x.getPolicySupplierFactory().createPolicySupplier(x.getPolicyId(), x.getPolicyCategoryId(), random.split()))
            .collect(Collectors.groupingBy(
                PolicySupplier::getPolicyCategoryId,
                Collectors.mapping(Function.identity(), Collectors.toList())))
            .entrySet()
            .stream()
            .map(x -> new PolicyCategory<>(x.getKey(), x.getValue()))
            .collect(Collectors.toList());

        var gameSampler = new GameSamplerImpl<>(
            evaluationArguments.getInitialStateSupplier(),
            evaluationArguments.getStateStateWrapperInitializer(),
            evaluationArguments.getEpisodeResultsFactory(),
            policyCategories,
            evaluationArguments.getProblemConfig().getPolicyShuffleStrategy(),
            systemConfig.isSingleThreadedEvaluation() ? 1 : systemConfig.getParallelThreadsCount(),
            evaluationArguments.getProblemConfig().getPolicyCategoryInfoList(),
            random.split());
        long start = System.currentTimeMillis();
        var episodeList = gameSampler.sampleEpisodes(systemConfig.getEvalEpisodeCount(), evaluationArguments.getProblemConfig().getMaximalStepCountBound(), PolicyMode.INFERENCE);
        long end = System.currentTimeMillis();
        logger.info("Evaluation of [{}] policy in [{}] runs took [{}] milliseconds", evaluationArguments.getRunName(), systemConfig.getEvalEpisodeCount(), end - start);
        var duration = Duration.ofMillis(end - start);

        if(systemConfig.dumpEvaluationData()) {
            evaluationArguments.getEpisodeWriter().writeEvaluationEpisode(episodeList);
        }
        return new ImmutableTuple<>(evaluationArguments.getEpisodeStatisticsCalculator().calculateStatistics(episodeList, duration), duration);
    }
}
