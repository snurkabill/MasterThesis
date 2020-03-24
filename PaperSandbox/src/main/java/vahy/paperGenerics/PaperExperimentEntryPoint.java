package vahy.paperGenerics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.observation.FixedModelObservation;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.config.PaperAlgorithmConfig;
import vahy.impl.benchmark.BenchmarkedPolicy;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.PolicyInferenceEvaluator;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.experiment.EpisodeWriter;
import vahy.impl.experiment.PolicyTrainingCycle;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.learning.trainer.Trainer;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.predictor.TrainableApproximator;
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
import vahy.paperGenerics.reinforcement.learning.tf.TFModelImproved;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.paperGenerics.selector.RiskBasedSelector_V1;
import vahy.paperGenerics.selector.RiskBasedSelector_V2;
import vahy.paperGenerics.selector.RiskBasedSelector_V3;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTriple;
import vahy.utils.ImmutableTuple;
import vahy.utils.ReflectionHacks;
import vahy.vizualiation.MyShittyFrameVisualization;
import vahy.vizualiation.ProgressTrackerSettings;
import vahy.vizualiation.XYDatasetBuilder;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaperExperimentEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(PaperExperimentEntryPoint.class);

    public static <
        TConfig extends ProblemConfig,
        TAction extends Enum<TAction> & Action<TAction>,
        TOpponentObservation extends FixedModelObservation<TAction>,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    ImmutableTriple<List<List<PaperEpisodeStatistics>>, List<PaperEpisodeStatistics>, List<BenchmarkedPolicy<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>>> createExperimentAndRun(
        Class<TAction> actionClass,
        BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TConfig, TAction, DoubleVector, TOpponentObservation, TState>> instanceInitializerFactory,
        Class<?> environmentPolicySupplier,
        List<PaperAlgorithmConfig> algorithmConfigList,
        SystemConfig systemConfig,
        TConfig problemConfig,
        Path resultPath) {

        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

        PaperEpisodeResultsFactory<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> episodeResultsFactory = new PaperEpisodeResultsFactory<>();

        List<TrainablePredictor> predictorList = new ArrayList<>();
        List<PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> policySupplierList = new ArrayList<>();
        List<SplittableRandom> masterRandomList = new ArrayList<>();
        List<List<PaperEpisodeStatistics>> trainingStatisticsList = new ArrayList<>();
        List<PaperEpisodeStatistics> evaluationStatisticsList = new ArrayList<>();
        List<EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> episodeWriterList = new ArrayList<>();
        List<BenchmarkedPolicy<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> benchmarkedPolicyList = new ArrayList<>();

        int policyId = 0;
        for (PaperAlgorithmConfig algorithmConfig : algorithmConfigList) {

            var policyName = "PolicyId_" + policyId;
            logger.info("Training policy id: [{}]", policyId);
            final var finalRandomSeed = systemConfig.getRandomSeed();
            final var masterRandom = new SplittableRandom(finalRandomSeed);

            logger.info("First random number for policy id: [{}] is [{}]", policyId, masterRandom.nextInt());

            var episodeWriter = new EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>(problemConfig, algorithmConfig, systemConfig, resultPath, timestamp, policyName);

            var strategiesProvider = new StrategiesProvider<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>(
                algorithmConfig.getInferenceExistingFlowStrategy(),
                algorithmConfig.getInferenceNonExistingFlowStrategy(),
                algorithmConfig.getExplorationExistingFlowStrategy(),
                algorithmConfig.getExplorationNonExistingFlowStrategy(),
                algorithmConfig.getFlowOptimizerType(),
                algorithmConfig.getSubTreeRiskCalculatorTypeForKnownFlow(),
                algorithmConfig.getSubTreeRiskCalculatorTypeForUnknownFlow(),
                algorithmConfig.getNoiseStrategy());

            PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier = createOpponentSupplier(environmentPolicySupplier, masterRandom);

            ImmutableTuple<TAction[], TAction[]> playerOpponentActions = getPlayerOpponentActions(actionClass);
            var initialStateSupplier = instanceInitializerFactory.apply(problemConfig, masterRandom.split());

            TrainablePredictor predictor = initializePredictor(
                    initialStateSupplier.createInitialState().getPlayerObservation().getObservedVector().length,
                    algorithmConfig,
                    systemConfig,
                    playerOpponentActions.getFirst().length,
                    masterRandom);

            Supplier<RiskAverseNodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>> nodeSelectorSupplier = createNodeSelectorSupplier(masterRandom, algorithmConfig, playerOpponentActions.getFirst().length);
            PaperMetadataFactory<TAction, DoubleVector, TOpponentObservation, TState> searchNodeMetadataFactory = new PaperMetadataFactory<>();
            NodeEvaluator<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> evaluator = resolveEvaluator(
                algorithmConfig,
                new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
                playerOpponentActions.getFirst(),
                playerOpponentActions.getSecond(),
                predictor,
                FixedModelObservation::getProbabilities,
                masterRandom
            );

            PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> policySupplier = new PaperPolicySupplier<>(
                actionClass,
                searchNodeMetadataFactory,
                algorithmConfig.getGlobalRiskAllowed(),
                masterRandom.split(),
                nodeSelectorSupplier,
                evaluator,
                new PaperTreeUpdater<>(),
                algorithmConfig.getTreeUpdateConditionFactory(),
                strategiesProvider,
                algorithmConfig.getExplorationConstantSupplier(),
                algorithmConfig.getTemperatureSupplier(),
                algorithmConfig.getRiskSupplier()
            );
            var progressTrackerSettings = new ProgressTrackerSettings(true, systemConfig.isDrawWindow(), false, false);
            var episodeDataMaker = new PaperEpisodeDataMaker<TAction, TOpponentObservation, TState, PaperPolicyRecord>(algorithmConfig.getDiscountFactor());
            var trainerAlgorithm = algorithmConfig.getDataAggregationAlgorithm();
            var gameSampler = new GameSamplerImpl<>(initialStateSupplier, episodeResultsFactory, PolicyMode.TRAINING, systemConfig.getParallelThreadsCount(), policySupplier, opponentPolicySupplier);
            var dataAggregator = trainerAlgorithm.resolveDataAggregator(algorithmConfig);
            var trainer = new Trainer<>(predictor, gameSampler, dataAggregator, episodeDataMaker, progressTrackerSettings, problemConfig,
                new PaperEpisodeStatisticsCalculator<>(),
                List.of(
                new DataPointGeneratorGeneric<>("Avg risk Hit", PaperEpisodeStatistics::getRiskHitRatio),
                new DataPointGeneratorGeneric<>("Stdev risk Hit", PaperEpisodeStatistics::getRiskHitStdev)
            ));

            var trainingCycle = new PolicyTrainingCycle<>(
                systemConfig,
                algorithmConfig,
                episodeWriter,
                trainer);

            var durationWithStatistics = trainingCycle.startTraining();;
            logger.info("Training duration: [{}] ms", durationWithStatistics.getFirst().toMillis());

            predictorList.add(predictor);
            policySupplierList.add(policySupplier);
            masterRandomList.add(masterRandom);
            episodeWriterList.add(episodeWriter);
            trainingStatisticsList.add(durationWithStatistics.getSecond());
            policyId++;
        }

        for (int i = 0; i < predictorList.size(); i++) {
            final var masterRandom = masterRandomList.get(i);
            PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier = createOpponentSupplier(environmentPolicySupplier, masterRandom);
            var initialStateSupplier = instanceInitializerFactory.apply(problemConfig, masterRandom.split());
            var episodeWriter = episodeWriterList.get(i);
            var benchmarkedPolicy = new BenchmarkedPolicy<>(("PolicyId_" + i), policySupplierList.get(i));
            benchmarkedPolicyList.add(benchmarkedPolicy);
            logger.info("Benchmarking policy [{}]", benchmarkedPolicy.getPolicyName());
            var policyResults = policyInferenceEvaluation(problemConfig, systemConfig, opponentPolicySupplier, initialStateSupplier, episodeResultsFactory, episodeWriter, benchmarkedPolicy);

            evaluationStatisticsList.add(policyResults.getEpisodeStatistics());
            logger.info(policyResults.getEpisodeStatistics().printToLog());
        }

        for (TrainablePredictor trainablePredictor : predictorList) {
            try {
                trainablePredictor.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        createReport(trainingStatisticsList, evaluationStatisticsList, benchmarkedPolicyList.stream().map(BenchmarkedPolicy::getPolicyName).collect(Collectors.toList()), systemConfig);
        return new ImmutableTriple<>(trainingStatisticsList, evaluationStatisticsList, benchmarkedPolicyList);
    }


    private static void createReport(List<List<PaperEpisodeStatistics>> trainingStatisticsList, List<PaperEpisodeStatistics> evaluationStatisticsList, List<String> names, SystemConfig systemConfig) {

        List<List<Double>> averageRewardList = extractElement(trainingStatisticsList, EpisodeStatisticsBase::getTotalPayoffAverage);
        List<List<Double>> stdevRewardList = extractElement(trainingStatisticsList, EpisodeStatisticsBase::getTotalPayoffStdev);
        List<List<Double>> riskRatioList = extractElement(trainingStatisticsList, PaperEpisodeStatistics::getRiskHitRatio);
        List<List<Double>> riskStdevList = extractElement(trainingStatisticsList, PaperEpisodeStatistics::getRiskHitStdev);

        if(systemConfig.isDrawWindow()) {
            String[] iterationArr = new String[4];
            String[] valueArr = new String[4];
            Arrays.fill(iterationArr, "Iteration");
            Arrays.fill(valueArr, "Value");
            MyShittyFrameVisualization myShittyFrameVisualization = new MyShittyFrameVisualization("Benchmark results", List.of("Average reward", "Stdev reward", "RiskRatio", "RiskStdev"), Arrays.asList(iterationArr), Arrays.asList(valueArr), Color.RED);

            var dataset1 = XYDatasetBuilder.createDatasetWithFixedX(averageRewardList, names);
            var dataset2 = XYDatasetBuilder.createDatasetWithFixedX(stdevRewardList, names);
            var dataset3 = XYDatasetBuilder.createDatasetWithFixedX(riskRatioList, names);
            var dataset4 = XYDatasetBuilder.createDatasetWithFixedX(riskStdevList, names);
            myShittyFrameVisualization.draw(List.of(dataset1, dataset2, dataset3, dataset4));
        }
    }

    private static List<List<Double>> extractElement(List<List<PaperEpisodeStatistics>> stats, Function<PaperEpisodeStatistics, Double> mapper) {
        return stats.stream().map(x -> x.stream().map(mapper).collect(Collectors.toList())).collect(Collectors.toList());
    }

    private static <TAction extends Enum<TAction> & Action<TAction>, TOpponentObservation extends FixedModelObservation<TAction>, TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> createOpponentSupplier(Class<?> environmentPolicySupplier, SplittableRandom masterRandom) {
        return (PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>) ReflectionHacks.createTypeInstance(
            environmentPolicySupplier,
            new Class[] {SplittableRandom.class},
            new Object[] {masterRandom});
    }


    private static <TConfig extends ProblemConfig, TAction extends Enum<TAction> & Action<TAction>, TOpponentObservation extends FixedModelObservation<TAction>, TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    PolicyResults<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord, PaperEpisodeStatistics>
    policyInferenceEvaluation(ProblemConfig problemConfig,
                              SystemConfig systemConfig,
                              PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier,
                              InitialStateSupplier<TConfig, TAction, DoubleVector, TOpponentObservation, TState> initialStateSupplier,
                              PaperEpisodeResultsFactory<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> episodeResultsFactory,
                              EpisodeWriter<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> episodeWriter,
                              BenchmarkedPolicy<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> benchmarkedPolicy)
    {
        logger.info("PaperPolicy test starts");
        var evaluator = new PolicyInferenceEvaluator<>(
            benchmarkedPolicy,
            opponentPolicySupplier,
            initialStateSupplier,
            episodeResultsFactory,
            new PaperEpisodeStatisticsCalculator<>()
        );
        long start = System.currentTimeMillis();
        var policyResultList = evaluator.runInferenceEvaluation(systemConfig.getEvalEpisodeCount(), problemConfig.getMaximalStepCountBound(), systemConfig.isSingleThreadedEvaluation() ? 1 : systemConfig.getParallelThreadsCount());
        long end = System.currentTimeMillis();

        if(systemConfig.dumpEvaluationData()) {
            episodeWriter.writeEvaluationEpisode(policyResultList.getEpisodeList());
        }
        var benchmarkingTime = end - start;
        logger.info("Benchmarking out-of-the-box took [{}] milliseconds", benchmarkingTime);
        return policyResultList;
    }

    private static <TAction extends Action<TAction>> ImmutableTuple<TAction[], TAction[]> getPlayerOpponentActions(Class<TAction> actionClass) {
        TAction[] values = ReflectionHacks.getEnumValues(actionClass);
        return new ImmutableTuple<>(values[0].getAllPlayerActions(), values[0].getAllOpponentActions());
    }

    private static TrainablePredictor initializePredictor(int modelInputSize,
                                                          PaperAlgorithmConfig algorithmConfig,
                                                          SystemConfig systemConfig,
                                                          int actionCount,
                                                          SplittableRandom masterRandom) {
        var approximatorType = algorithmConfig.getApproximatorType();
        var defaultPrediction = new double[2 + actionCount];
        defaultPrediction[0] = 0;
        defaultPrediction[1] = 0.0;
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i + 2] = 1.0 / actionCount;
        }
        try {
            switch(approximatorType) {
                case EMPTY:
                    return new EmptyPredictor(defaultPrediction);
                case HASHMAP:
                    return new DataTablePredictor(defaultPrediction);
                case HASHMAP_LR:
                    return new DataTablePredictorWithLr(defaultPrediction, algorithmConfig.getLearningRate(), actionCount);
                case TF_NN:
                    var tfModelAsBytes = createTensorFlowModel(algorithmConfig, systemConfig, modelInputSize, actionCount);
//                var tfModel = new TFModel(
//                    modelInputSize,
//                    PaperModel.POLICY_START_INDEX + actionCount,
//                    algorithmConfig.getTrainingEpochCount(),
//                    algorithmConfig.getTrainingBatchSize(),
//                    tfModelAsBytes,
//                    masterRandom.split());
                    var tfModel = new TFModelImproved(
                        modelInputSize,
                        PaperModel.POLICY_START_INDEX + actionCount,
                        algorithmConfig.getTrainingBatchSize(),
                        algorithmConfig.getTrainingEpochCount(),
                        tfModelAsBytes,
                        systemConfig.getParallelThreadsCount(),
                        masterRandom.split());
                    return new TrainableApproximator(tfModel);
                case DL4J_NN:
                    var model = new Dl4jModel(
                        modelInputSize,
                        PaperModel.POLICY_START_INDEX + actionCount,
                        null,
                        masterRandom.nextInt(),
                        algorithmConfig.getLearningRate(),
                        algorithmConfig.getTrainingEpochCount(),
                        algorithmConfig.getTrainingBatchSize());
                    return new TrainableApproximator(model);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(approximatorType);
            }
        } catch (IOException |InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] createTensorFlowModel(PaperAlgorithmConfig algorithmConfig, SystemConfig systemConfig, int inputCount, int outputActionCount) throws IOException, InterruptedException {
        var modelName = "tfModel_" + DateTime.now().withZone(DateTimeZone.UTC);
        Process process = Runtime.getRuntime().exec(systemConfig.getPythonVirtualEnvPath() +
            " PythonScripts/tensorflow_models/" +
            algorithmConfig.getCreatingScript() +
            " " +
            modelName +
            " " +
            inputCount +
            " " +
            outputActionCount +
            " PythonScripts/generated_models" +
            " " +
            (int)systemConfig.getRandomSeed());

        try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            String line2;

            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            while ((line2 = error.readLine()) != null) {
                System.out.println(line2);
            }
        }
        var exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new IllegalStateException("Python process ended with non-zero exit value. Exit val: [" + exitValue + "]");
        }
        var dir = new File("PythonScripts/generated_models/");
        Files.createDirectories(dir.toPath());
        return Files.readAllBytes(new File(dir, modelName + ".pb").toPath());
    }

    private static <TAction extends Enum<TAction> & Action<TAction>, TOpponentObservation extends Observation, TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
        Supplier<RiskAverseNodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>>
    createNodeSelectorSupplier(SplittableRandom masterRandom, PaperAlgorithmConfig algorithmConfig, int playerTotalActionCount)
    {
        var cpuctParameter = algorithmConfig.getCpuctParameter();
        var selectorType = algorithmConfig.getSelectorType();
        var totalRiskAllowed = algorithmConfig.getGlobalRiskAllowed();
        switch (selectorType) {
            case UCB:
                return () -> new PaperNodeSelector<>(cpuctParameter, masterRandom.split());
            case RISK_AVERSE_UCB_V3:
                return () -> new RiskBasedSelector_V3<>(cpuctParameter, masterRandom.split(), playerTotalActionCount);
            case RISK_AVERSE_UCB_V2_EXPERIMENTAL:
                logger.warn("Node selector: [" + RiskBasedSelector_V2.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector_V2<>(cpuctParameter, masterRandom.split());
            case RISK_AVERSE_UCB_V1_EXPERIMENTAL:
                logger.warn("Node selector: [" + RiskBasedSelector_V1.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector_V1<>(cpuctParameter, masterRandom.split(), totalRiskAllowed);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }

    private static <
        TAction extends Enum<TAction> & Action<TAction>,
        TOpponentObservation extends FixedModelObservation<TAction>,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    NodeEvaluator<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> resolveEvaluator(
        PaperAlgorithmConfig algorithmConfig,
        SearchNodeFactory<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> searchNodeFactory,
        TAction[] playerActions,
        TAction[] opponentActions,
        TrainablePredictor approximator,
        Function<TOpponentObservation, ImmutableTuple<List<TAction>, List<Double>>> opponentPredictor,
        SplittableRandom masterRandom
        )
    {
        var evaluatorType = algorithmConfig.getEvaluatorType();
        var discountFactor = algorithmConfig.getDiscountFactor();
        var batchedEvaluationSize = algorithmConfig.getBatchedEvaluationSize();

        switch (evaluatorType) {
            case RALF:
                return new PaperNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    opponentPredictor,
                    playerActions,
                    opponentActions);
            case RALF_BATCHED:
                return new PaperBatchNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    opponentPredictor,
                    playerActions,
                    opponentActions,
                    batchedEvaluationSize);
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    opponentPredictor,
                    playerActions,
                    opponentActions,
                    masterRandom.split(),
                    discountFactor);
            case RAMCP:
                return new RamcpNodeEvaluator<>(
                    searchNodeFactory,
                    opponentPredictor,
                    playerActions,
                    opponentActions,
                    masterRandom.split(),
                    discountFactor);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }
}

