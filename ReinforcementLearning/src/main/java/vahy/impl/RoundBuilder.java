package vahy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatisticsCalculator;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.episode.StateWrapperInitializer;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.PolicyRecord;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicy;
import vahy.impl.runner.EpisodeWriter;
import vahy.impl.runner.EvaluationArguments;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.runner.Runner;
import vahy.impl.runner.RunnerArguments;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoundBuilder<TConfig extends ProblemConfig, TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>, TPolicyRecord extends PolicyRecord, TStatistics extends EpisodeStatisticsBase> {

    private static final Logger logger = LoggerFactory.getLogger(RoundBuilder.class);

    public static final long EVALUATION_SEED_SHIFT = 100_000;
    public static final int ENVIRONMENT_CATEGORY_ID = 0;

    private String roundName;
    private String timestamp;
    private boolean dumpData;
    private TConfig problemConfig;
    private SystemConfig systemConfig;
    private CommonAlgorithmConfig commonAlgorithmConfig;

    private List<PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord>> playerPolicyArgumentList;

    private EpisodeStatisticsCalculator<TAction, DoubleVector, TState, TPolicyRecord, TStatistics> statisticsCalculator;
    private EpisodeResultsFactory<TAction, DoubleVector, TState, TPolicyRecord> resultsFactory;

    private BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TState>> instanceInitializerFactory;
    private StateWrapperInitializer<TAction, DoubleVector, TState> stateStateWrapperInitializer;

    private List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList;

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setRoundName(String roundName) {
        this.roundName = roundName;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setProblemConfig(TConfig problemConfig) {
        this.problemConfig = problemConfig;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setCommonAlgorithmConfig(CommonAlgorithmConfig commonAlgorithmConfig) {
        this.commonAlgorithmConfig = commonAlgorithmConfig;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setPlayerPolicySupplierList(List<PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord>> policyDefinitionList) {
        this.playerPolicyArgumentList = policyDefinitionList;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setStatisticsCalculator(EpisodeStatisticsCalculator<TAction, DoubleVector, TState, TPolicyRecord, TStatistics> statisticsCalculator) {
        this.statisticsCalculator = statisticsCalculator;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setResultsFactory(EpisodeResultsFactory<TAction, DoubleVector, TState, TPolicyRecord> resultsFactory) {
        this.resultsFactory = resultsFactory;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setAdditionalDataPointGeneratorListSupplier(List<DataPointGeneratorGeneric<TStatistics>> additionalDataPointGeneratorList) {
        this.additionalDataPointGeneratorList = additionalDataPointGeneratorList;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setProblemInstanceInitializerSupplier(BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TState>> instanceInitializerFactory) {
        this.instanceInitializerFactory = instanceInitializerFactory;
        return this;
    }

    public RoundBuilder<TConfig, TAction, TState, TPolicyRecord, TStatistics> setStateStateWrapperInitializer(StateWrapperInitializer<TAction, DoubleVector, TState> stateStateWrapperInitializer) {
        this.stateStateWrapperInitializer  = stateStateWrapperInitializer;
        return this;
    }

    private void finalizeSetup() {
        if(roundName == null) {
            throw new IllegalArgumentException("Missing RunName");
        }
        if(systemConfig == null) {
            throw new IllegalArgumentException("Missing systemConfig");
        }
        if(problemConfig == null) {
            throw new IllegalArgumentException("Missing problemConfig");
        }
        if(commonAlgorithmConfig == null) {
            throw new IllegalArgumentException("Missing commonAlgorithmConfig");
        }
        if(instanceInitializerFactory == null) {
            throw new IllegalArgumentException("Missing instanceInitializerFactory");
        }
        if(stateStateWrapperInitializer == null) {
            throw new IllegalArgumentException("Missing stateStateWrapperInitializer");
        }
        if(statisticsCalculator == null) {
            throw new IllegalArgumentException("Missing statisticsCalculator");
        }
        if(resultsFactory == null) {
            throw new IllegalArgumentException("Missing resultsFactory");
        }
        if(playerPolicyArgumentList == null) {
            throw new IllegalArgumentException("Missing policyArgumentList");
        }
        checkPolicyArgumentList(playerPolicyArgumentList);
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        logger.info("Finalized setup with timestamp [{}]", timestamp);
        dumpData = (systemConfig.dumpEvaluationData() || systemConfig.dumpTrainingData());
    }

    private void checkPolicyArgumentList(List<PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord>> policyDefinitionList) {
        Set<Integer> policyIdSet = new HashSet<>();
        for (PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord> entry : policyDefinitionList) {
            if(policyIdSet.contains(entry.getPolicyId())) {
                throw new IllegalStateException("Two or more policies have policy id: [" + entry.getPolicyId() + "]");
            } else {
                policyIdSet.add(entry.getPolicyId());
            }
            if(entry.getCategoryId() == ENVIRONMENT_CATEGORY_ID) {
                throw new IllegalArgumentException("Category ID: [" + ENVIRONMENT_CATEGORY_ID + "] is reserved for environment and hence can't be used as player category.");
            }
            if(entry.getCategoryId() < 0) {
                throw new IllegalArgumentException("All category Ids should be positive. Got: [" + entry.getCategoryId() + "]");
            }
        }
    }

    public PolicyResults<TAction, DoubleVector, TState, TPolicyRecord, TStatistics> execute() {
        finalizeSetup();
        var runner = new Runner<TConfig, TAction, DoubleVector, TState, TPolicyRecord, TStatistics>();
        try {
            var episodeWriter = dumpData ? new EpisodeWriter<TAction, DoubleVector, TState, TPolicyRecord>(problemConfig, commonAlgorithmConfig, systemConfig, timestamp, roundName) : null;
            var runnerArguments = buildRunnerArguments(episodeWriter);
            var evaluationArguments = buildEvaluationArguments(episodeWriter);
            return runner.run(runnerArguments, evaluationArguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RunnerArguments<TConfig, TAction, DoubleVector, TState, TPolicyRecord, TStatistics> buildRunnerArguments(EpisodeWriter<TAction, DoubleVector, TState, TPolicyRecord> episodeWriter) {
        final var finalRandomSeed = systemConfig.getRandomSeed();
        final var masterRandom = new SplittableRandom(finalRandomSeed);

        var policyList = createEnvironmentPolicySuppliers(problemConfig);
        policyList.addAll(playerPolicyArgumentList);

        return new RunnerArguments<>(
            roundName,
            problemConfig,
            systemConfig,
            commonAlgorithmConfig,
            masterRandom,
            instanceInitializerFactory.apply(problemConfig, masterRandom.split()),
            stateStateWrapperInitializer,
            resultsFactory,
            statisticsCalculator,
            additionalDataPointGeneratorList,
            episodeWriter,
            policyList);
    }

    private EvaluationArguments<TConfig, TAction, DoubleVector, TState, TPolicyRecord, TStatistics> buildEvaluationArguments(EpisodeWriter<TAction, DoubleVector, TState, TPolicyRecord> episodeWriter) {
        final var finalRandomSeed = systemConfig.getRandomSeed();
        final var masterRandom = new SplittableRandom(finalRandomSeed + EVALUATION_SEED_SHIFT);

        var policyList = createEnvironmentPolicySuppliers(problemConfig);
        policyList.addAll(playerPolicyArgumentList);

        return new EvaluationArguments<>(
            roundName,
            problemConfig,
            systemConfig,
            masterRandom,
            instanceInitializerFactory.apply(this.problemConfig, masterRandom.split()),
            stateStateWrapperInitializer,
            resultsFactory,
            statisticsCalculator,
            episodeWriter,
            policyList);
    }

    private List<PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord>> createEnvironmentPolicySuppliers(ProblemConfig config) {
        return IntStream.range(0, config.getEnvironmentPolicyCount())
            .mapToObj(x ->
                new PolicyDefinition<TAction, DoubleVector, TState, TPolicyRecord>(
                    x,
                    ENVIRONMENT_CATEGORY_ID,
                    (initialState, policyMode, policyId, random) -> new KnownModelPolicy<>(random, policyId),
                    new ArrayList<>()
                ))
            .collect(Collectors.toList());
    }

}
