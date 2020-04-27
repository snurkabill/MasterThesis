package vahy.paperGenerics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ApproximatorConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.Predictor;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.config.PaperAlgorithmConfig;
import vahy.impl.benchmark.Benchmark;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.learning.trainer.OpponentSamplerDataMaker;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicySupplier;
import vahy.impl.predictor.DataTableDistributionPredictorWithLr;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.runner.EpisodeWriter;
import vahy.impl.runner.EvaluationArguments;
import vahy.impl.runner.Runner;
import vahy.impl.runner.RunnerArguments;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.benchmark.PaperEpisodeStatisticsCalculator;
import vahy.paperGenerics.evaluator.MonteCarloNodeEvaluator;
import vahy.paperGenerics.evaluator.PaperBatchNodeEvaluator;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.evaluator.RamcpNodeEvaluator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.DataTablePredictorWithLr;
import vahy.paperGenerics.reinforcement.episode.PaperEpisodeResultsFactory;
import vahy.paperGenerics.reinforcement.learning.PaperEpisodeDataMaker;
import vahy.paperGenerics.reinforcement.learning.dl4j.Dl4jModel;
import vahy.paperGenerics.reinforcement.learning.tf.TFModel;
import vahy.paperGenerics.reinforcement.learning.tf.TFModelImproved;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.paperGenerics.selector.RiskBasedSelector_V1;
import vahy.paperGenerics.selector.RiskBasedSelector_V2;
import vahy.paperGenerics.selector.RiskBasedSelector_V3;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;
import vahy.utils.ReflectionHacks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class PaperExperimentBuilder<
    TConfig extends ProblemConfig,
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(PaperExperimentBuilder.class);

    private static final long EVALUATION_SEED_SHIFT = 100_000;

    private String timestamp;
    private boolean dumpData;
    private Class<TAction> actionClazz;
    private Class<TState> stateClazz;
    private TConfig problemConfig;
    private SystemConfig systemConfig;
    private List<PaperAlgorithmConfig> algorithmConfigList;

    private BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TOpponentObservation, TState>> instanceInitializerFactory;
    private Function<SplittableRandom, PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> opponentPolicyCreator = KnownModelPolicySupplier::new;

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setStateClass(Class<TState> stateClass) {
        this.stateClazz = stateClass;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setActionClass(Class<TAction> actionClass) {
        this.actionClazz = actionClass;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setProblemConfig(TConfig problemConfig) {
        this.problemConfig = problemConfig;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setAlgorithmConfigList(List<PaperAlgorithmConfig> algorithmConfigList) {
        this.algorithmConfigList = algorithmConfigList;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setOpponentSupplier(Function<SplittableRandom, PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> creator) {
        this.opponentPolicyCreator = creator;
        return this;
    }

    public PaperExperimentBuilder<TConfig, TAction, TOpponentObservation, TState> setProblemInstanceInitializerSupplier(BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TOpponentObservation, TState>> instanceInitializerFactory) {
        this.instanceInitializerFactory = instanceInitializerFactory;
        return this;
    }

    private void finalizeSetup() {
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        logger.info("Finalized setup with timestamp [{}]", timestamp);
        dumpData = (systemConfig.dumpEvaluationData() || systemConfig.dumpTrainingData());
        if(stateClazz == null) {
            throw new IllegalArgumentException("Missing StateClass. Don't forget to add it within ExperimentBuilder");
        }
        if(actionClazz == null) {
            throw new IllegalArgumentException("Missing ActionClass. Don't forget to add it within ExperimentBuilder");
        }
        if(instanceInitializerFactory == null) {
            throw new IllegalArgumentException("Missing instanceInitializerFactory");
        }
    }

    public List<PolicyResults<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics>> execute() {
        finalizeSetup();
        List<PolicyResults<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics>> resultList = new ArrayList<>(algorithmConfigList.size());
        var runner = new Runner<TConfig, TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics>();
        try {
            for (int i = 0; i < algorithmConfigList.size(); i++) {
                var policyId = algorithmConfigList.get(i).getAlgorithmId();
                var episodeWriter = dumpData ? new EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>(problemConfig, algorithmConfigList.get(i), systemConfig, timestamp, policyId) : null;
                var runnerArguments = buildRunnerArguments(algorithmConfigList.get(i), episodeWriter, policyId);
                var evaluationArguments = buildEvaluationArguments(episodeWriter);
                resultList.add(runner.run(runnerArguments, evaluationArguments));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var benchmark = new Benchmark<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics>(
            List.of(
                new ImmutableTuple<>(EpisodeStatistics::getTotalPayoffAverage, "Payoff average"),
                new ImmutableTuple<>(EpisodeStatistics::getTotalPayoffStdev, "Payoff stdev"),
                new ImmutableTuple<>(EpisodeStatistics::getAveragePlayerStepCount, "Player step count average"),
                new ImmutableTuple<>(EpisodeStatistics::getStdevPlayerStepCount, "Player step count stdev"),
                new ImmutableTuple<>(EpisodeStatistics::getAverageMillisPerEpisode, "Per episode ms average"),
                new ImmutableTuple<>(EpisodeStatistics::getStdevMillisPerEpisode, "Per episode ms stdev"),
                new ImmutableTuple<>(PaperEpisodeStatistics::getRiskHitRatio, "Risk hit average"),
                new ImmutableTuple<>(PaperEpisodeStatistics::getRiskHitStdev, "Risk hit stdev")
            ),
            systemConfig);
        benchmark.benchmark(resultList);

        return resultList;
    }

    private RunnerArguments<TConfig, TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics> buildRunnerArguments(PaperAlgorithmConfig algorithmConfig,
                                                                                                                                                          EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> episodeWriter,
                                                                                                                                                          String policyId) {
        final var finalRandomSeed = systemConfig.getRandomSeed();
        final var masterRandom = new SplittableRandom(finalRandomSeed);

        var playerOpponentActions = getPlayerOpponentActions(actionClazz);

        var playerActionCount = playerOpponentActions.getFirst().length;
        var opponentActionCount = playerOpponentActions.getSecond().length;

        var stateFactory = instanceInitializerFactory.apply(problemConfig, masterRandom.split());
        var initialStateSample = stateFactory.createInitialState(PolicyMode.INFERENCE);
        var observedVectorLength = initialStateSample.getPlayerObservation().getObservedVector().length;

        // TODO this is dirty.
        var playerPredictorSetup = createPlayerTrainingSetup(playerActionCount, observedVectorLength, algorithmConfig.getDiscountFactor(), algorithmConfig.getPlayerApproximatorConfig(), masterRandom.split());
        var knownPredictor = problemConfig.isModelKnown() ? initialStateSample.getKnownModelWithPerfectObservationPredictor() : null;
        var opponentPredictorSetup = knownPredictor == null ? createOpponentTrainingSetup(opponentActionCount, observedVectorLength, algorithmConfig.getOpponentApproximatorConfig(), masterRandom.split()) : null;
        var opponentPredictor = opponentPredictorSetup == null ? null : opponentPredictorSetup.getTrainablePredictor();
        var policySupplier = createPolicySupplier(algorithmConfig, masterRandom.split(), playerOpponentActions, playerPredictorSetup.getTrainablePredictor(), opponentPredictor , knownPredictor);

        var trainablePredictorSetupList = new ArrayList<>(List.of(
            playerPredictorSetup
        ));
        if(opponentPredictorSetup != null) {
            trainablePredictorSetupList.add(opponentPredictorSetup);
        }

        return new RunnerArguments<>(
            policyId,
            problemConfig,
            systemConfig,
            algorithmConfig,
            instanceInitializerFactory.apply(problemConfig, masterRandom.split()),
            new PaperEpisodeResultsFactory<>(),
            new PaperEpisodeStatisticsCalculator<>(),
            List.of(
                new DataPointGeneratorGeneric<>("Risk hit average", PaperEpisodeStatistics::getRiskHitRatio),
                new DataPointGeneratorGeneric<>("Risk hit stdev", PaperEpisodeStatistics::getRiskHitStdev)
            ),
            opponentPolicyCreator.apply(masterRandom.split()),
            policySupplier,
            episodeWriter,
            trainablePredictorSetupList
        );
    }

    private EvaluationArguments<TConfig, TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics> buildEvaluationArguments(EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> episodeWriter) {
        final var finalRandomSeed = systemConfig.getRandomSeed();
        final var masterRandom = new SplittableRandom(finalRandomSeed + EVALUATION_SEED_SHIFT);
        return new EvaluationArguments<>(
            problemConfig,
            systemConfig,
            instanceInitializerFactory.apply(this.problemConfig, masterRandom.split()),
            new PaperEpisodeResultsFactory<>(),
            new PaperEpisodeStatisticsCalculator<>(),
            opponentPolicyCreator.apply(masterRandom.split()),
            episodeWriter
        );
    }

    private ImmutableTuple<TAction[], TAction[]> getPlayerOpponentActions(Class<TAction> actionClass) {
        TAction[] values = ReflectionHacks.getEnumValues(actionClass);
        Object[] ref = Arrays.stream(values).filter(Action::isPlayerAction).toArray();
        Object[] ref2 = Arrays.stream(values).filter(Action::isOpponentAction).toArray();
        return new ImmutableTuple<>(Arrays.copyOf(ref, ref.length, ReflectionHacks.arrayClassFromClass(actionClass)), Arrays.copyOf(ref2, ref2.length, ReflectionHacks.arrayClassFromClass(actionClass)));
    }

    private PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> createPolicySupplier(PaperAlgorithmConfig algorithmConfig,
                                                                                                                        SplittableRandom masterRandom,
                                                                                                                        ImmutableTuple<TAction[], TAction[]> playerOpponentActions,
                                                                                                                        TrainablePredictor playerPredictor,
                                                                                                                        TrainablePredictor opponentPredictor,
                                                                                                                        Predictor<TState> knownPredictor)
    {
        var nodeEvaluator = resolveEvaluator(
            algorithmConfig,
            new SearchNodeBaseFactoryImpl<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>(actionClazz, new PaperMetadataFactory<>(actionClazz)),
            playerOpponentActions.getFirst(),
            playerOpponentActions.getSecond(),
            playerPredictor,
            opponentPredictor,
            knownPredictor,
            masterRandom.split());

        var nodeSelectorSupplier = createNodeSelectorSupplier(masterRandom, algorithmConfig, playerOpponentActions.getFirst().length);

        var strategiesProvider = new StrategiesProvider<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>(
            actionClazz,
            algorithmConfig.getInferenceExistingFlowStrategy(),
            algorithmConfig.getInferenceNonExistingFlowStrategy(),
            algorithmConfig.getExplorationExistingFlowStrategy(),
            algorithmConfig.getExplorationNonExistingFlowStrategy(),
            algorithmConfig.getFlowOptimizerType(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForKnownFlow(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForUnknownFlow(),
            algorithmConfig.getNoiseStrategy());

        return new PaperPolicySupplier<>(
            actionClazz,
            new PaperMetadataFactory<>(actionClazz),
            algorithmConfig.getGlobalRiskAllowed(),
            masterRandom.split(),
            nodeSelectorSupplier,
            nodeEvaluator,
            new PaperTreeUpdater<>(),
            algorithmConfig.getTreeUpdateConditionFactory(),
            strategiesProvider,
            algorithmConfig.getExplorationConstantSupplier(),
            algorithmConfig.getTemperatureSupplier(),
            algorithmConfig.getRiskSupplier());
    }

    private PredictorTrainingSetup<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> createOpponentTrainingSetup(int opponentActionCount,
                                                                                                                                       int observedVectorLength,
                                                                                                                                       ApproximatorConfig approximatorConfig,
                                                                                                                                       SplittableRandom masterRandom) {
        TrainablePredictor opponentPredictor = initializeOpponentPredictor(observedVectorLength, approximatorConfig, systemConfig, opponentActionCount, masterRandom.split());
        return new PredictorTrainingSetup<>(
            opponentPredictor,
            new OpponentSamplerDataMaker<TAction, TOpponentObservation, TState, PaperPolicyRecord>(opponentActionCount),
            approximatorConfig.getDataAggregationAlgorithm().resolveDataAggregator(approximatorConfig)
        );
    }

    private PredictorTrainingSetup<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> createPlayerTrainingSetup(int playerActionCount,
                                                                                                                                     int observedVectorLength,
                                                                                                                                     double discountFactor,
                                                                                                                                     ApproximatorConfig approximatorConfig,
                                                                                                                                     SplittableRandom masterRandom) {
        TrainablePredictor playerPredictor = initializePlayerPredictor(observedVectorLength, approximatorConfig, systemConfig, playerActionCount, masterRandom.split());
        return new PredictorTrainingSetup<>(
            playerPredictor,
            new PaperEpisodeDataMaker<TAction, TOpponentObservation, TState, PaperPolicyRecord>(discountFactor),
            approximatorConfig.getDataAggregationAlgorithm().resolveDataAggregator(approximatorConfig)
        );
    }

    private Supplier<RiskAverseNodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>> createNodeSelectorSupplier(SplittableRandom masterRandom,
                                                                                                                                                     PaperAlgorithmConfig algorithmConfig,
                                                                                                                                                     int playerTotalActionCount)
    {
        var cpuctParameter = algorithmConfig.getCpuctParameter();
        var selectorType = algorithmConfig.getSelectorType();
        var totalRiskAllowed = algorithmConfig.getGlobalRiskAllowed();
        switch (selectorType) {
            case UCB:
                return () -> new PaperNodeSelector<>(cpuctParameter, masterRandom.split(), playerTotalActionCount);
            case RISK_AVERSE_UCB_V3:
                return () -> new RiskBasedSelector_V3<>(cpuctParameter, masterRandom.split(), playerTotalActionCount);
            case RISK_AVERSE_UCB_V2_EXPERIMENTAL:
                logger.warn("Node selector: [" + RiskBasedSelector_V2.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector_V2<>(cpuctParameter, masterRandom.split(), playerTotalActionCount);
            case RISK_AVERSE_UCB_V1_EXPERIMENTAL:
                logger.warn("Node selector: [" + RiskBasedSelector_V1.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector_V1<>(cpuctParameter, masterRandom.split(), playerTotalActionCount, totalRiskAllowed);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }

    private NodeEvaluator<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> resolveEvaluator(PaperAlgorithmConfig algorithmConfig,
                                                                                                                        SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> searchNodeFactory,
                                                                                                                        TAction[] playerActions,
                                                                                                                        TAction[] opponentActions,
                                                                                                                        Predictor<DoubleVector> approximator,
                                                                                                                        Predictor<DoubleVector> opponentPredictor,
                                                                                                                        Predictor<TState> knownModel,
                                                                                                                        SplittableRandom masterRandom)
    {
        var evaluatorType = algorithmConfig.getEvaluatorType();
        var discountFactor = algorithmConfig.getDiscountFactor();
        var batchedEvaluationSize = algorithmConfig.getBatchedEvaluationSize();

        if(evaluatorType == null) {
            throw new IllegalArgumentException("Missing evaluator type");
        }

        TState[] array = (TState[]) java.lang.reflect.Array.newInstance(stateClazz, 0);

//        var array = ReflectionHacks.arrayFromGenericClass(stateClazz, 10);

        switch (evaluatorType) {
            case RALF:
                return new PaperNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    opponentPredictor,
                    knownModel,
                    playerActions,
                    opponentActions);
            case RALF_BATCHED:
                return new PaperBatchNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    opponentPredictor,
                    knownModel,
                    playerActions,
                    opponentActions,
                    batchedEvaluationSize,
                    array
                    );
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    knownModel,
                    playerActions,
                    opponentActions,
                    masterRandom.split(),
                    discountFactor);
            case RAMCP:
                return new RamcpNodeEvaluator<>(
                    searchNodeFactory,
                    knownModel,
                    playerActions,
                    opponentActions,
                    masterRandom.split(),
                    discountFactor);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }

    private TrainablePredictor initializePlayerPredictor(int modelInputSize, ApproximatorConfig approximatorConfig, SystemConfig systemConfig, int actionCount, SplittableRandom masterRandom)
    {
        var defaultPrediction = new double[2 + actionCount];
        defaultPrediction[0] = 0;
        defaultPrediction[1] = 0.0;
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i + 2] = 1.0 / actionCount;
        }
        return createPredictor(modelInputSize, actionCount, approximatorConfig, systemConfig, masterRandom, defaultPrediction, false);
    }

    private TrainablePredictor initializeOpponentPredictor(int modelInputSize, ApproximatorConfig approximatorConfig, SystemConfig systemConfig, int actionCount, SplittableRandom masterRandom)
    {
        var defaultPrediction = new double[actionCount];
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i] = 1.0 / actionCount;
        }
        return createPredictor(modelInputSize, actionCount, approximatorConfig, systemConfig, masterRandom, defaultPrediction, true);
    }

    private TrainablePredictor createPredictor(int modelInputSize, int actionCount, ApproximatorConfig approximatorConfig, SystemConfig systemConfig, SplittableRandom masterRandom, double[] defaultPrediction, boolean isOpponent) {
        if(approximatorConfig == null) {
            throw new IllegalArgumentException("ApproximatorConfig type is not defined. Either player or model predictor setting is missing.");
        }
        var approximatorType = approximatorConfig.getApproximatorType();
        try {
            switch(approximatorType) {
                case EMPTY:
                    return new EmptyPredictor(defaultPrediction);
                case HASHMAP:
                    return new DataTablePredictor(defaultPrediction);
                case HASHMAP_LR:
                    return isOpponent ?
                        new DataTableDistributionPredictorWithLr(defaultPrediction, approximatorConfig.getLearningRate())
                        : new DataTablePredictorWithLr(defaultPrediction, approximatorConfig.getLearningRate(), actionCount);
                case TF_NN:
                    var tfModelAsBytes = loadTensorFlowModel(approximatorConfig, systemConfig, modelInputSize, actionCount);
                    var tfModel = new TFModelImproved(
                        modelInputSize,
                        defaultPrediction.length,
                        approximatorConfig.getTrainingBatchSize(),
                        approximatorConfig.getTrainingEpochCount(),
                        approximatorConfig.getDropoutKeepProbability(),
                        approximatorConfig.getLearningRate(),
                        tfModelAsBytes,
                        systemConfig.getParallelThreadsCount(),
                        masterRandom.split());
                    return new TrainableApproximator(tfModel);
                case TF_OLD_NN:
                    var tfModelAsBytes2 = loadTensorFlowModel(approximatorConfig, systemConfig, modelInputSize, actionCount);
                    var tfModel2 = new TFModel(
                        modelInputSize,
                        defaultPrediction.length,
                        approximatorConfig.getTrainingEpochCount(),
                        approximatorConfig.getTrainingBatchSize(),
                        tfModelAsBytes2,
                        masterRandom.split());
                    return new TrainableApproximator(tfModel2);
                case DL4J_NN:
                    var model = new Dl4jModel(
                        modelInputSize,
                        defaultPrediction.length,
                        null,
                        masterRandom.nextInt(),
                        approximatorConfig.getLearningRate(),
                        approximatorConfig.getTrainingEpochCount(),
                        approximatorConfig.getTrainingBatchSize());
                    return new TrainableApproximator(model);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(approximatorType);
            }
        } catch (IOException |InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] loadTensorFlowModel(ApproximatorConfig approximatorConfig, SystemConfig systemConfig, int inputCount, int outputActionCount) throws IOException, InterruptedException {
        var modelName = "tfModel_" + DateTime.now().withZone(DateTimeZone.UTC);
        Process process = Runtime.getRuntime().exec(systemConfig.getPythonVirtualEnvPath()
            + " " +
            Paths.get("PythonScripts", "tensorflow_models", approximatorConfig.getCreatingScript()) +
            " " +
            modelName +
            " " +
            inputCount +
            " " +
            outputActionCount +
            " " +
            Paths.get("PythonScripts", "generated_models") +
            " " +
            (int)systemConfig.getRandomSeed());

        try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            String line2;

            while ((line = input.readLine()) != null) {
                logger.info(line);
            }
            while ((line2 = error.readLine()) != null) {
                logger.error(line2);
            }
        }
        var exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new IllegalStateException("Python process ended with non-zero exit value. Exit val: [" + exitValue + "]");
        }
        var dir = new File(Paths.get("PythonScripts", "generated_models").toString());
        Files.createDirectories(dir.toPath());
        return Files.readAllBytes(new File(dir, modelName + ".pb").toPath());
    }

}
