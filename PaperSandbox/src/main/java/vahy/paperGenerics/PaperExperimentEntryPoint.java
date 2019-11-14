package vahy.paperGenerics;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.observation.FixedModelObservation;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.policy.PolicySupplier;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.config.PaperAlgorithmConfig;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class PaperExperimentEntryPoint {

    public static <
        TConfig extends ProblemConfig,
        TAction extends Enum<TAction> & Action,
        TOpponentObservation extends FixedModelObservation<TAction>,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    void createExperimentAndRun(Class<TAction> actionClass,
                                Class<TOpponentObservation> opponentObservationClass,
                                Class<TState> stateClass,
                                Class<InitialStateSupplier<TConfig, TAction, DoubleVector, TOpponentObservation, TState>> initialInstanceSupplierClass,
                                Class opponentPolicyClass,
                                PaperAlgorithmConfig algorithmConfig,
                                SystemConfig systemConfig,
                                TConfig problemConfig) {

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

        AbstractExperiment<TConfig, TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> experiment = createExperimentInstance();
        ImmutableTuple<TAction[], TAction[]> playerOpponentActions = getPlayerOpponentActions(actionClass);

        try {
            TrainablePredictor approximator = initializePredictor(
                0,
                algorithmConfig,
                playerOpponentActions.getFirst().length,
                null,
                masterRandom,
                finalRandomSeed
            );
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
//
//            Supplier<PolicySupplier> opponentSupplier = () -> {
//                try {
//                    return opponentPolicyClass.getConstructor().newInstance(masterRandom.split());
//                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException k) {
//                    throw new RuntimeException(k);
//                };
//            };


//            try {
//                var initialStateSupplier = ;
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }

            var initialStateSupplier = ReflectionHacks.createTypeInstance(initialInstanceSupplierClass, new Class[] {SplittableRandom.class}, new Object[] {masterRandom.split()});
            PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord> opponentPolicySupplier = (PolicySupplier<TAction, DoubleVector, TOpponentObservation, TState, PaperPolicyRecord>) ReflectionHacks.createTypeInstance(opponentPolicyClass, new Class[] {SplittableRandom.class}, new Object[] {masterRandom.split()});

            experiment.run(
                policySupplier,
                opponentPolicySupplier,
                new PaperEpisodeResultsFactory<>(),
                initialStateSupplier,
                new ProgressTrackerSettings(true, systemConfig.isDrawWindow(), false, false),
                null,
                approximator,
                new PaperEpisodeDataMaker<>(algorithmConfig.getDiscountFactor()),
                new PaperEpisodeStatisticsCalculator<>(),
                new EpisodeWriter<>(problemConfig, algorithmConfig, systemConfig, Path.of("Results")),
                systemConfig,
                    algorithmConfig
                );

            approximator.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <TAction> ImmutableTuple<TAction[], TAction[]> getPlayerOpponentActions(Class<TAction> actionClass) {
        String playerActionsMethodName = "getAllPlayerActions";
        String opponentActionsMethodName = "getAllOpponentActions";
        try {
            TAction[] values = (TAction[]) actionClass.getMethod("values", null).invoke(null, null);
            TAction[] playerActions = ReflectionHacks.invokeMethod(values[0], playerActionsMethodName, null,null);
            TAction[] opponentActions = ReflectionHacks.invokeMethod(values[0], opponentActionsMethodName, null,null);
            return new ImmutableTuple<>(playerActions, opponentActions);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static <
        TConfig extends ProblemConfig,
        TAction extends Enum<TAction> & Action,
        TOpponentObservation extends Observation,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>,
        TPolicyRecord extends PolicyRecord>
    AbstractExperiment<TConfig, TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> createExperimentInstance() {
        return ReflectionHacks.createTypeInstance(AbstractExperiment.class, null, null); // AbstractExperiment.class.getConstructor().newInstance(null);

    }

    private static TrainablePredictor initializePredictor(int modelInputSize,
                                                          PaperAlgorithmConfig algorithmConfig,
                                                          int allPlayerActionCount,
                                                          byte[] tfModelAsByes,
                                                          SplittableRandom masterRandom,
                                                          long finalRandomSeed) {
        var inputLenght = modelInputSize;
        var approximatorType = algorithmConfig.getApproximatorType();

        var actionCount = allPlayerActionCount;
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
                var tfModel = new TFModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + actionCount,
                    algorithmConfig.getTrainingEpochCount(),
                    algorithmConfig.getTrainingBatchSize(),
                    tfModelAsByes,
                    masterRandom.split());
                return new TrainableApproximator(tfModel);
            case DL4J_NN:
                var model = new Dl4jModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + actionCount,
                    null,
                    finalRandomSeed,
                    algorithmConfig.getLearningRate(),
                    algorithmConfig.getTrainingEpochCount(),
                    algorithmConfig.getTrainingBatchSize());
                return new TrainableApproximator(model);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(approximatorType);
        }
    }

    private static <
        TAction extends Enum<TAction> & Action,
        TOpponentObservation extends Observation,
        TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>>
    Supplier<NodeSelector<TAction, DoubleVector, TOpponentObservation, PaperMetadata<TAction>, TState>>
    createNodeSelectorSupplier(SplittableRandom masterRandom, PaperAlgorithmConfig algorithmConfig)
    {
        var random = masterRandom.split();
        var cpuctParameter = algorithmConfig.getCpuctParameter();
        var selectorType = algorithmConfig.getSelectorType();
        var totalRiskAllowed = algorithmConfig.getGlobalRiskAllowed();
        switch (selectorType) {
            case UCB:
                return () -> new PaperNodeSelector<>(cpuctParameter, random);
            case VAHY_1:
//                logger.warn("Node selector: [" + RiskBasedSelectorVahy.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelectorVahy<>(cpuctParameter, random);
            case LINEAR_HARD_VS_UCB:
//                logger.warn("Node selector: [" + RiskBasedSelector.class.getName() + "] is considered Experimental.");
                return () -> new RiskBasedSelector<>(cpuctParameter, random.split(), totalRiskAllowed);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }

    private static <
        TAction extends Enum<TAction> & Action,
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
