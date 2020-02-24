package vahy.paperGenerics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.observation.FixedModelObservation;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.config.PaperAlgorithmConfig;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.episode.FromEpisodesDataPointGeneratorGeneric;
import vahy.impl.experiment.AbstractExperiment;
import vahy.impl.experiment.EpisodeWriter;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
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
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.paperGenerics.selector.RiskBasedSelector;
import vahy.paperGenerics.selector.RiskBasedSelectorVahy;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;
import vahy.utils.ReflectionHacks;
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class PaperExperimentEntryPoint {

    public static <
        TConfig extends ProblemConfig,
        TAction extends Enum<TAction> & Action<TAction>,
        TOpponentObservation extends FixedModelObservation<TAction>,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    List<PolicyResults<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> createExperimentAndRun(
        Class<TAction> actionClass,
        BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TConfig, TAction, DoubleVector, TOpponentObservation, TState>> instanceInitializerFactory,
        Class<?> environmentPolicySupplier,
//        Function<SplittableRandom, PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> opponentInitializerFactory,
        PaperAlgorithmConfig algorithmConfig,
        SystemConfig systemConfig,
        TConfig problemConfig,
        Path resultPath) {


//        TOTO JEDE
//        Class<Policy<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> castedEnvironmentClass = (Class<Policy<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>>) environmentPolicyClass;
////        Class<PaperPolicy<TAction, DoubleVector, TOpponentObservation, TState>> castedEnvironmentClass = environmentPolicyClass.getClass();
//
//        Function<SplittableRandom, PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>> opponentInitializerFactory =
//            splittableRandom -> (initialState, policyMode) -> ReflectionHacks.createTypeInstance(castedEnvironmentClass, new Class[] {SplittableRandom.class}, new Object[] {splittableRandom});


        var finalRandomSeed = systemConfig.getRandomSeed();
        var masterRandom = new SplittableRandom(finalRandomSeed);
        var strategiesProvider = new StrategiesProvider<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>(
            algorithmConfig.getInferenceExistingFlowStrategy(),
            algorithmConfig.getInferenceNonExistingFlowStrategy(),
            algorithmConfig.getExplorationExistingFlowStrategy(),
            algorithmConfig.getExplorationNonExistingFlowStrategy(),
            algorithmConfig.getFlowOptimizerType(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForKnownFlow(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForUnknownFlow());


        PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier = (PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>)ReflectionHacks.createTypeInstance(environmentPolicySupplier, new Class[] {SplittableRandom.class}, new Object[] {masterRandom});

        var experiment = (AbstractExperiment<TConfig, TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>) ReflectionHacks.createTypeInstance(AbstractExperiment.class, null, null);
        ImmutableTuple<TAction[], TAction[]> playerOpponentActions = getPlayerOpponentActions(actionClass);
        var initialStateSupplier = instanceInitializerFactory.apply(problemConfig, masterRandom.split());

        try {
            try(TrainablePredictor approximator = initializePredictor(
                initialStateSupplier.createInitialState().getPlayerObservation().getObservedVector().length,
                algorithmConfig,
                systemConfig,
                playerOpponentActions.getFirst().length,
                masterRandom))
            {
                Supplier<NodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>> nodeSelectorSupplier = createNodeSelectorSupplier(masterRandom, algorithmConfig);
                PaperMetadataFactory<TAction, DoubleVector, TOpponentObservation, TState> searchNodeMetadataFactory = new PaperMetadataFactory<>();
                NodeEvaluator<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState> evaluator = resolveEvaluator(
                    algorithmConfig,
                    new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
                    playerOpponentActions.getFirst(),
                    playerOpponentActions.getSecond(),
                    approximator,
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


//                PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier = opponentInitializerFactory.apply(masterRandom);

                return experiment.run(
                    policySupplier,
                    opponentPolicySupplier,
                    new PaperEpisodeResultsFactory<>(),
                    initialStateSupplier,
                    new ProgressTrackerSettings(true, systemConfig.isDrawWindow(), false, false),
                    Collections.singletonList(new FromEpisodesDataPointGeneratorGeneric<>("Risk Hit", episodeResults -> episodeResults.stream().mapToDouble(x -> x.getFinalState().isRiskHit() ? 1 : 0).average().orElseThrow())),
                    approximator,
                    new PaperEpisodeDataMaker<>(algorithmConfig.getDiscountFactor()),
                    new PaperEpisodeStatisticsCalculator<>(),
                    new EpisodeWriter<>(problemConfig, algorithmConfig, systemConfig, resultPath),
                    systemConfig,
                    algorithmConfig
                );
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <TAction extends Action<TAction>> ImmutableTuple<TAction[], TAction[]> getPlayerOpponentActions(Class<TAction> actionClass) {
        TAction[] values = ReflectionHacks.getEnumValues(actionClass);
        return new ImmutableTuple<>(values[0].getAllPlayerActions(), values[0].getAllOpponentActions());
    }

    private static TrainablePredictor initializePredictor(int modelInputSize,
                                                          PaperAlgorithmConfig algorithmConfig,
                                                          SystemConfig systemConfig,
                                                          int actionCount,
                                                          SplittableRandom masterRandom) throws IOException, InterruptedException {
        var approximatorType = algorithmConfig.getApproximatorType();
        var defaultPrediction = new double[2 + actionCount];
        defaultPrediction[0] = 0;
        defaultPrediction[1] = 0.0;
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i + 2] = 1.0 / actionCount;
        }

        switch(approximatorType) {
            case EMPTY:
                return new EmptyPredictor(defaultPrediction);
            case HASHMAP:
                return new DataTablePredictor(defaultPrediction);
            case HASHMAP_LR:
                return new DataTablePredictorWithLr(defaultPrediction, algorithmConfig.getLearningRate(), actionCount);
            case TF_NN:
                var tfModelAsBytes = createTensorFlowModel(algorithmConfig, systemConfig, modelInputSize, actionCount);
                var tfModel = new TFModel(
                    modelInputSize,
                    PaperModel.POLICY_START_INDEX + actionCount,
                    algorithmConfig.getTrainingEpochCount(),
                    algorithmConfig.getTrainingBatchSize(),
                    tfModelAsBytes,
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
            " PythonScripts/generated_models");

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

    private static <
        TAction extends Enum<TAction> & Action<TAction>,
        TOpponentObservation extends Observation,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    Supplier<NodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>>
    createNodeSelectorSupplier(SplittableRandom masterRandom, PaperAlgorithmConfig algorithmConfig)
    {
        var cpuctParameter = algorithmConfig.getCpuctParameter();
        var selectorType = algorithmConfig.getSelectorType();
        var totalRiskAllowed = algorithmConfig.getGlobalRiskAllowed();
        switch (selectorType) {
            case UCB:
                return () -> new PaperNodeSelector<>(cpuctParameter, masterRandom.split());
            case VAHY_1:
//                logger.warn("Node selector: [" + RiskBasedSelectorVahy.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelectorVahy<>(cpuctParameter, masterRandom.split());
            case LINEAR_HARD_VS_UCB:
//                logger.warn("Node selector: [" + RiskBasedSelector.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector<>(cpuctParameter, masterRandom.split(), totalRiskAllowed);
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

