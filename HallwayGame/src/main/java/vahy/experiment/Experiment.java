package vahy.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.PaperGenericsPrototype;
import vahy.api.model.Action;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.config.AlgorithmConfig;
import vahy.config.SystemConfig;
import vahy.environment.HallwayAction;
import vahy.environment.agent.policy.environment.EnvironmentPolicySupplier;
import vahy.environment.config.GameConfig;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.HallwayGameSupplierFactory;
import vahy.game.HallwayInstance;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.paperGenerics.MonteCarloNodeEvaluator;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperNodeEvaluator;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.RamcpNodeEvaluator;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.experiment.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.DataTableApproximator;
import vahy.paperGenerics.reinforcement.DataTableApproximatorWithLr;
import vahy.paperGenerics.reinforcement.EmptyApproximator;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.paperGenerics.reinforcement.learning.AbstractTrainer;
import vahy.paperGenerics.reinforcement.learning.EveryVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.FirstVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.ReplayBufferTrainer;
import vahy.paperGenerics.reinforcement.learning.tf.TFModel;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.paperGenerics.selector.RiskBasedSelector;
import vahy.paperGenerics.selector.RiskBasedSelectorVahy;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class Experiment {

    private final Logger logger = LoggerFactory.getLogger(Experiment.class);

    private final AlgorithmConfig algorithmConfig;
    private final SystemConfig systemConfig;
    private final SplittableRandom masterRandom;
    private final long finalRandomSeed;

    // general
    private DoubleScalarRewardAggregator rewardAggregator = new DoubleScalarRewardAggregator();
    private ProgressTrackerSettings progressTrackerSettings;



    private HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier;

    // HEURISTICS
    private StrategiesProvider<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> strategiesProvider;


    // NODE
    private PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> paperTreeUpdater;
    private PaperMetadataFactory<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> searchNodeMetadataFactory;
    private SearchNodeBaseFactoryImpl<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> searchNodeFactory;
    private PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> nodeEvaluator;

    private TrainableApproximator<DoubleVector> approximator;

    // Domain specific
    private Class<HallwayAction> clazz;
    private HallwayAction[] allActions;
    private HallwayAction[] allPlayerActions;

    //Dirty hack
    private TFModel tfModel;

    private List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> results;

    public Experiment(AlgorithmConfig algorithmConfig, SystemConfig systemConfig) {
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

    private void initializeHelperClasses() {
        paperTreeUpdater = new PaperTreeUpdater<>();
        searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);
        searchNodeFactory = new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory);
        progressTrackerSettings = new ProgressTrackerSettings(true, systemConfig.isDrawWindow(), false, false);
        strategiesProvider = new StrategiesProvider<>(
            algorithmConfig.getInferenceExistingFlowStrategy(),
            algorithmConfig.getInferenceNonExistingFlowStrategy(),
            algorithmConfig.getExplorationExistingFlowStrategy(),
            algorithmConfig.getExplorationNonExistingFlowStrategy(),
            algorithmConfig.getFlowOptimizerType(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForKnownFlow(),
            algorithmConfig.getSubTreeRiskCalculatorTypeForUnknownFlow(),
            masterRandom.split());
    }

    public List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> getResults() {
        return results;
    }


    public void run(GameConfig gameConfig, HallwayInstance gameInstance) {
        try {
            initializeInstanceSupplier(gameConfig, gameInstance);
        } catch (IOException | NotValidGameStringRepresentationException e) {
            throw new RuntimeException("HallwayGame instance was not created.", e);
        }
        initializeHelperClasses();
        initializeDomainClasses();

        try {
            approximator = initializeApproximator("tfModel/graph_" + gameInstance.toString() + ".pb");
        } catch (IOException e) {
            throw new RuntimeException("TF model instance was not created.", e);
        }

        if(tfModel != null) { // dirty // TODO: better resources handling
            final TFModel tfModelFinal = tfModel;
            try(tfModelFinal) {
                resolveEvaluator();
            }
        } else {
            resolveEvaluator();
        }
        createPolicyAndRunProcess(gameConfig);
    }

    private TrainableApproximator<DoubleVector> initializeApproximator(String modelName) throws IOException {
        var inputLenght = hallwayGameInitialInstanceSupplier.createInitialState().getPlayerObservation().getObservedVector().length;
        var approximatorType = algorithmConfig.getApproximatorType();
        switch(approximatorType) {
            case EMPTY:
                return new EmptyApproximator<>();
            case HASHMAP:
                return new DataTableApproximator<>(allPlayerActions.length);
            case HASHMAP_LR:
                return new DataTableApproximatorWithLr<>(allPlayerActions.length, algorithmConfig.getLearningRate());
            case NN:
                tfModel = new TFModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
                    algorithmConfig.getTrainingEpochCount(),
                    algorithmConfig.getTrainingBatchSize(),
                    PaperGenericsPrototype.class.getClassLoader().getResourceAsStream(modelName).readAllBytes(),
                    masterRandom.split());
                return new TrainableApproximator<>(tfModel);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(approximatorType);
        }
    }

    private void createPolicyAndRunProcess(GameConfig gameConfig) {
        Supplier<NodeSelector<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> nodeSelector = this::createNodeSelector;
        var evaluator = resolveEvaluator();

        var paperTrainablePolicySupplier = new TrainablePaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            algorithmConfig.getGlobalRiskAllowed(),
            masterRandom.split(),
            nodeSelector,
            evaluator,
            paperTreeUpdater,
            algorithmConfig.getTreeUpdateConditionFactory(),
            algorithmConfig.getExplorationConstantSupplier(),
            algorithmConfig.getTemperatureSupplier(),
            algorithmConfig.getRiskSupplier(),
            strategiesProvider);

        var evaluationPolicySupplier = new PaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            algorithmConfig.getGlobalRiskAllowed(),
            masterRandom.split(),
            nodeSelector,
            evaluator,
            paperTreeUpdater,
            algorithmConfig.getTreeUpdateConditionFactory(),
            strategiesProvider);


        var trainer = getAbstractTrainer(evaluator, paperTrainablePolicySupplier);


        var startTrainingMillis = System.currentTimeMillis();
        trainPolicy(trainer);
        var endTrainingMillis = System.currentTimeMillis();
        var trainingDurationMillis = endTrainingMillis - startTrainingMillis;


        var policyList = Arrays.asList(new PaperBenchmarkingPolicy<>(evaluator.getClass().getName(), evaluationPolicySupplier));
        this.results = evaluatePolicy(policyList);
        printResults(policyList, trainingDurationMillis);

        try {
            dumpResults(results, gameConfig);
        } catch (IOException e) {
            logger.error("Results dump failed. ", e);
        }
    }

    private void printResults(List<PaperBenchmarkingPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> policyList, long trainingTimeInMs) {
        for (var policyEntry : policyList) {
            var nnResults = results
                .stream()
                .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(policyEntry.getPolicyName()))
                .findFirst()
                .get();
            logger.info("[{}]", nnResults.getCalculatedResultStatistics().printToLog());
            logger.info("Training time: [{}]ms", trainingTimeInMs);
            logger.info("Total time: [{}]ms", trainingTimeInMs + nnResults.getBenchmarkingMilliseconds());
        }

    }

    private void dumpResults(List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> results, GameConfig gameConfig) throws IOException {
        if(results.size() > 1) {
            throw new IllegalStateException("It is not implemented to benchmark multiple policies at the same time");
        }
        var policyResult = results.get(0);

        String resultMasterFolderName = "Results";
        File resultFolder = new File(resultMasterFolderName);

        if(!resultFolder.exists()) {
            resultFolder.mkdir();
        }

        File resultSubfolder = new File(resultFolder, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")));
        resultSubfolder.mkdir();

        File experimentSetuFile = new File(resultSubfolder, "algorithmConfig");
        PrintWriter out = new PrintWriter(experimentSetuFile);
        out.print("AlgorithmConfig: " + algorithmConfig.toString() + System.lineSeparator() + System.lineSeparator() + "GameSetup: " + gameConfig.toString());
        out.close();
        File resultFile = new File(resultSubfolder.getAbsolutePath(), "Rewards");
        writeEpisodeResultsToFile(resultFile.getAbsolutePath(), policyResult.getRewardAndRiskList());
    }

    public static void writeEpisodeResultsToFile(String filename, List<ImmutableTuple<Double, Boolean>> list) throws IOException{
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        outputWriter.write("Reward,Risk");
        outputWriter.newLine();
        for (int i = 0; i < list.size(); i++) {
            outputWriter.write(list.get(i).getFirst() + "," + list.get(i).getSecond());
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    private void trainPolicy(AbstractTrainer trainer) {
        for (int i = 0; i < algorithmConfig.getStageCount(); i++) {
            logger.info("Training policy for [{}]th iteration", i);
            trainer.trainPolicy(algorithmConfig.getBatchEpisodeCount());
//            trainer.printDataset();
        }
    }

    private List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> evaluatePolicy(
        List<PaperBenchmarkingPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> policyList)
    {
        logger.info("PaperPolicy test starts");
        var benchmark = new PaperBenchmark<>(
            policyList,
            new EnvironmentPolicySupplier(masterRandom.split()),
            hallwayGameInitialInstanceSupplier,
            progressTrackerSettings
        );
        long start = System.currentTimeMillis();
        var policyResultList = benchmark.runBenchmark(systemConfig.getEvalEpisodeCount(), algorithmConfig.getMaximalStepCountBound(), systemConfig.isSingleThreadedEvaluation() ? 1 : systemConfig.getParallelThreadsCount());
        long end = System.currentTimeMillis();
        var benchmarkingTime = end - start;
        logger.info("Benchmarking took [{}] milliseconds", benchmarkingTime);
        return policyResultList;
    }

    private AbstractTrainer<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> getAbstractTrainer(
        PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> nodeEvaluator,
        TrainablePaperPolicySupplier<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> trainablePaperPolicySupplier)
    {
        var environmentPolicySupplier = new EnvironmentPolicySupplier(masterRandom.split());
        var discountFactor = algorithmConfig.getDiscountFactor();
        var trainerAlgorithm = algorithmConfig.getTrainerAlgorithm();
        var stepCountLimit = algorithmConfig.getMaximalStepCountBound();
        int parallelThreadsCount = systemConfig.getParallelThreadsCount();
        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    environmentPolicySupplier,
                    nodeEvaluator,
                    discountFactor,
                    rewardAggregator,
                    stepCountLimit,
                    new LinkedList<>(),
                    algorithmConfig.getReplayBufferSize(),
                    progressTrackerSettings,
                    parallelThreadsCount);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    environmentPolicySupplier,
                    nodeEvaluator,
                    discountFactor,
                    rewardAggregator,
                    progressTrackerSettings,
                    stepCountLimit,
                    parallelThreadsCount);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    environmentPolicySupplier,
                    nodeEvaluator,
                    discountFactor,
                    rewardAggregator,
                    progressTrackerSettings,
                    stepCountLimit,
                    parallelThreadsCount);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);
        }
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

    private PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> resolveEvaluator()
    {
        var evaluatorType = algorithmConfig.getEvaluatorType();
        var discountFactor = algorithmConfig.getDiscountFactor();
        switch (evaluatorType) {
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    masterRandom.split(),
                    rewardAggregator,
                    discountFactor);
            case RALF:
                return new PaperNodeEvaluator<>(
                    searchNodeFactory,
                    approximator,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions);
            case RAMCP:
                return new RamcpNodeEvaluator<>(
                    searchNodeFactory,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    masterRandom.split(),
                    rewardAggregator,
                    discountFactor);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }
}
