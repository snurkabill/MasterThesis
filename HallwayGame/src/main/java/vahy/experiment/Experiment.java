package vahy.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.PaperGenericsPrototype;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.policy.PolicyMode;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.config.PaperAlgorithmConfig;
import vahy.environment.HallwayAction;
import vahy.environment.agent.policy.environment.EnvironmentPolicySupplier;
import vahy.environment.config.GameConfig;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.HallwayGameSupplierFactory;
import vahy.game.HallwayInstance;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.benchmark.BenchmarkedPolicy;
import vahy.impl.benchmark.PolicyBenchmark;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.experiment.PolicyTrainingCycle;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.learning.trainer.Trainer;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperTreeUpdater;
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
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class Experiment {

    private final Logger logger = LoggerFactory.getLogger(Experiment.class);

    private final PaperAlgorithmConfig algorithmConfig;
    private final SystemConfig systemConfig;
    private final SplittableRandom masterRandom;
    private final long finalRandomSeed;
    private GameConfig gameConfig;

    // general
    private ProgressTrackerSettings progressTrackerSettings;


    private HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier;

    // HEURISTICS
    private StrategiesProvider<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> strategiesProvider;


    // NODE
    private PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> paperTreeUpdater;
    private PaperMetadataFactory<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> searchNodeMetadataFactory;
    private SearchNodeBaseFactoryImpl<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> searchNodeFactory;
    private PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> nodeEvaluator;

    private TrainablePredictor approximator;

    // Domain specific
    private Class<HallwayAction> clazz;
    private HallwayAction[] allActions;
    private HallwayAction[] allPlayerActions;

    //Dirty hack
    private TFModel tfModel;

    // Writer
    private EpisodeWriter episodeWriter;

    private List<PolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> results;

    public Experiment(PaperAlgorithmConfig algorithmConfig, SystemConfig systemConfig) {
        this.algorithmConfig = algorithmConfig;
        this.systemConfig = systemConfig;
        var finalRandomSeed = systemConfig.getRandomSeed();
        this.masterRandom = new SplittableRandom(finalRandomSeed);
        this.finalRandomSeed = finalRandomSeed;
    }

    private void initializeDomainClasses() {
        clazz = HallwayAction.class;
        allActions = clazz.getEnumConstants();
        allPlayerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).toArray(HallwayAction[]::new);
    }

    private void initializeInstanceSupplier(GameConfig gameConfig, HallwayInstance instance) throws IOException, NotValidGameStringRepresentationException {
        var provider = new HallwayGameSupplierFactory();
        this.hallwayGameInitialInstanceSupplier = provider.getInstanceProvider(instance, gameConfig, masterRandom.split());
    }

    private void initializeHelperClasses(GameConfig gameConfig) {
        episodeWriter = new EpisodeWriter(gameConfig, algorithmConfig);
        paperTreeUpdater = new PaperTreeUpdater<>();
        searchNodeMetadataFactory = new PaperMetadataFactory<>();
        searchNodeFactory = new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory);
        progressTrackerSettings = new ProgressTrackerSettings(true, systemConfig.isDrawWindow(), false, false);
        strategiesProvider = new StrategiesProvider<>(
            algorithmConfig.getInferenceExistingFlowStrategy(),
            algorithmConfig.getInferenceNonExistingFlowStrategy(),
            algorithmConfig.getExplorationExistingFlowStrategy(),
            algorithmConfig.getExplorationNonExistingFlowStrategy(),
            algorithmConfig.getFlowOptimizerType(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForKnownFlow(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForUnknownFlow());
    }

    public List<PolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> getResults() {
        return results;
    }


    public void run(GameConfig gameConfig, HallwayInstance gameInstance) {
        this.gameConfig = gameConfig;
        try {
            initializeInstanceSupplier(gameConfig, gameInstance);
        } catch (IOException | NotValidGameStringRepresentationException e) {
            throw new RuntimeException("HallwayGame instance was not created.", e);
        }
        initializeHelperClasses(gameConfig);
        initializeDomainClasses();

        try {
            approximator = initializePredictor("tfModel/graph_" + gameInstance.toString() + ".pb");
        } catch (IOException e) {
            throw new RuntimeException("TF model instance was not created.", e);
        }

        if(tfModel != null) { // dirty // TODO: better resources handling
            final TFModel tfModelFinal = tfModel;
            try(tfModelFinal) {
                resolveEvaluatorAndRun();
            }
        } else {
            resolveEvaluatorAndRun();
        }

    }

    private TrainablePredictor initializePredictor(String modelName) throws IOException {
        var inputLenght = hallwayGameInitialInstanceSupplier.createInitialState().getPlayerObservation().getObservedVector().length;
        var approximatorType = algorithmConfig.getApproximatorType();

        var actionCount = allPlayerActions.length;
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
                tfModel = new TFModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
                    algorithmConfig.getTrainingEpochCount(),
                    algorithmConfig.getTrainingBatchSize(),
                    PaperGenericsPrototype.class.getClassLoader().getResourceAsStream(modelName).readAllBytes(),
                    masterRandom.split());
                return new TrainableApproximator(tfModel);
            case DL4J_NN:
                var model = new Dl4jModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
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

    private void createPolicyAndRunProcess(
        NodeEvaluator<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> evaluator) {
        Supplier<NodeSelector<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> nodeSelector = this::createNodeSelector;

        var paperPolicySupplier = new PaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            algorithmConfig.getGlobalRiskAllowed(),
            masterRandom.split(),
            nodeSelector,
            evaluator,
            paperTreeUpdater,
            algorithmConfig.getTreeUpdateConditionFactory(),
            strategiesProvider,
            algorithmConfig.getExplorationConstantSupplier(),
            algorithmConfig.getTemperatureSupplier(),
            algorithmConfig.getRiskSupplier());

        var trainer = getAbstractTrainer(paperPolicySupplier);

        String resultMasterFolderName = "Results";
        File tempPathDeleteMe = new File(resultMasterFolderName, resultMasterFolderName);

        var masterPolicyTrainer = new PolicyTrainingCycle<>(
            systemConfig,
            algorithmConfig,
            new vahy.impl.experiment.EpisodeWriter<>(gameConfig, algorithmConfig, systemConfig, tempPathDeleteMe.toPath()),
            trainer
        );
        var trainingDuration = masterPolicyTrainer.executeTrainingPolicy();

        var policyList = Arrays.asList(new BenchmarkedPolicy<>(evaluator.getClass().getName(), paperPolicySupplier));
        this.results = evaluatePolicy(policyList);
        printResults(policyList, trainingDuration);

        if(results.size() > 1) {
            throw new IllegalStateException("It is not implemented to benchmark multiple policies at the same time");
        }
        episodeWriter.writeEvaluationEpisode(results.get(0).getEpisodeList());
    }

    private void printResults(List<BenchmarkedPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> policyList, Duration trainingDuration) {
        for (var policyEntry : policyList) {
            var nnResults = results
                .stream()
                .filter(x -> x.getPolicy().getPolicyName().equals(policyEntry.getPolicyName()))
                .findFirst()
                .get();
            logger.info("[{}]", nnResults.getEpisodeStatistics().printToLog());
            logger.info("Training time: [{}]ms", trainingDuration.toMillis());
            logger.info("Total time: [{}]ms", trainingDuration.toMillis() + nnResults.getBenchmarkingDuration().toMillis());
        }
    }

//    private void trainPolicy(PaperTrainer trainer) {
//        for (int i = 0; i < algorithmConfig.getStageCount(); i++) {
//            logger.info("Training policy for [{}]th iteration", i);
//            var episodes = trainer.trainPolicy(algorithmConfig.getBatchEpisodeCount(), algorithmConfig.getMaximalStepCountBound());
//            if(systemConfig.dumpTrainingData()) {
//                episodeWriter.writeTrainingEpisode(i, episodes);
//            }
//        }
//    }

    private List<PolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> evaluatePolicy(
        List<BenchmarkedPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> policyList)
    {
        logger.info("PaperPolicy test starts");
        var benchmark = new PolicyBenchmark<>(
            policyList,
            new EnvironmentPolicySupplier(masterRandom.split()),
            hallwayGameInitialInstanceSupplier,
            new PaperEpisodeResultsFactory<>(),
            new PaperEpisodeStatisticsCalculator<>(),
            progressTrackerSettings,
            null
        );
        long start = System.currentTimeMillis();
        var policyResultList = benchmark.runBenchmark(systemConfig.getEvalEpisodeCount(), algorithmConfig.getMaximalStepCountBound(), systemConfig.isSingleThreadedEvaluation() ? 1 : systemConfig.getParallelThreadsCount());
        long end = System.currentTimeMillis();
        var benchmarkingTime = end - start;
        logger.info("Benchmarking out-of-the-box took [{}] milliseconds", benchmarkingTime);
        return policyResultList;
    }

    private Trainer<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord> getAbstractTrainer(
        PaperPolicySupplier<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> paperPolicySupplier)
    {
        var discountFactor = algorithmConfig.getDiscountFactor();
        var trainerAlgorithm = algorithmConfig.getDataAggregationAlgorithm();

        var gameSampler = new GameSamplerImpl<>(
            hallwayGameInitialInstanceSupplier,
            new PaperEpisodeResultsFactory<>(),
            PolicyMode.TRAINING,
            progressTrackerSettings,
            systemConfig.getParallelThreadsCount(),
            paperPolicySupplier,
            new EnvironmentPolicySupplier(masterRandom.split()),
            null
        );

        var dataAggregator = trainerAlgorithm.resolveDataAggregator(algorithmConfig);
        return new Trainer<>(approximator, gameSampler, dataAggregator, new PaperEpisodeDataMaker<>(discountFactor));
    }

    private NodeSelector<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> createNodeSelector()
    {
        var random = masterRandom.split();
        var cpuctParameter = algorithmConfig.getCpuctParameter();
        var selectorType = algorithmConfig.getSelectorType();
        var totalRiskAllowed = algorithmConfig.getGlobalRiskAllowed();
        switch (selectorType) {
            case UCB:
                return new PaperNodeSelector<>(cpuctParameter, random);
            case VAHY_1:
                logger.warn("Node selector: [" + RiskBasedSelectorVahy.class.getName() + "] is considered Experimental.");
                return new RiskBasedSelectorVahy<>(cpuctParameter, random);
            case LINEAR_HARD_VS_UCB:
                logger.warn("Node selector: [" + RiskBasedSelector.class.getName() + "] is considered Experimental.");
                return new RiskBasedSelector<>(cpuctParameter, random.split(), totalRiskAllowed);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }

    private void resolveEvaluatorAndRun(){
        createPolicyAndRunProcess(resolveEvaluator());
        logger.info("DONE!!!");
    }

    private NodeEvaluator<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> resolveEvaluator() {
        var evaluatorType = algorithmConfig.getEvaluatorType();
        var discountFactor = algorithmConfig.getDiscountFactor();
        var batchedEvaluationSize = algorithmConfig.getBatchedEvaluationSize();
        switch (evaluatorType) {
            case RALF:
                return new PaperNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions);
            case RALF_BATCHED:
                return new PaperBatchNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    batchedEvaluationSize);
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    masterRandom.split(),
                    discountFactor);
            case RAMCP:
                return new RamcpNodeEvaluator<>(
                    searchNodeFactory,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    masterRandom.split(),
                    discountFactor);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }
}
