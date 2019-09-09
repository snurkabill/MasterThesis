package vahy.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.PaperGenericsPrototype;
import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.data.HallwayGameSupplierFactory;
import vahy.environment.HallwayAction;
import vahy.environment.config.GameConfig;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.paperGenerics.MonteCarloNodeEvaluator;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperNodeEvaluator;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.RamcpNodeEvaluator;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.experiment.EvaluatorType;
import vahy.paperGenerics.experiment.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.policy.environment.EnvironmentPolicySupplier;
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
import vahy.riskBasedSearch.RiskBasedSelector;
import vahy.riskBasedSearch.RiskBasedSelectorVahy;
import vahy.riskBasedSearch.SelectorType;
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

    private List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> results;

    public List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> getResults() {
        return results;
    }

    public void prepareAndRun(ImmutableTuple<GameConfig, ExperimentSetup> setup, SplittableRandom random) throws NotValidGameStringRepresentationException, IOException {
        initializeModelAndRun(setup, random);
    }

    private void initializeModelAndRun(ImmutableTuple<GameConfig, ExperimentSetup> setup, SplittableRandom random) throws NotValidGameStringRepresentationException, IOException {
        var provider = new HallwayGameSupplierFactory();
        var hallwayGameInitialInstanceSupplier = provider.getInstanceProvider(setup.getSecond().getHallwayInstance(), setup.getFirst(), random);
        var inputLenght = hallwayGameInitialInstanceSupplier.createInitialState().getPlayerObservation().getObservedVector().length;

        switch (setup.getSecond().getApproximatorType()) {
            case EMPTY:
                createPolicyAndRunProcess(setup, random.split(), hallwayGameInitialInstanceSupplier, new EmptyApproximator<>());
                break;
            case HASHMAP:
                createPolicyAndRunProcess(setup, random.split(), hallwayGameInitialInstanceSupplier, new DataTableApproximator<>(HallwayAction.playerActions.length, setup.getSecond().omitProbabilities()));
                break;
            case HASHMAP_LR:
                createPolicyAndRunProcess(setup, random.split(), hallwayGameInitialInstanceSupplier, new DataTableApproximatorWithLr<>(HallwayAction.playerActions.length, setup.getSecond().getLearningRate(), setup.getSecond().omitProbabilities()));
                break;
            case NN:
            {
                try(TFModel model = new TFModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
                    setup.getSecond().getTrainingEpochCount(),
                    setup.getSecond().getTrainingBatchSize(),
                    PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/graph_" + setup.getSecond().getHallwayInstance().toString() + ".pb").readAllBytes(),
                    random.split())
                ) //            SavedModelBundle.load("C:/Users/Snurka/init_model", "serve"),
                {
                    TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);
                    createPolicyAndRunProcess(setup, random.split(), hallwayGameInitialInstanceSupplier, trainableApproximator);
                }
            }
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(setup.getSecond().getApproximatorType());
        }
    }

    private void createPolicyAndRunProcess(ImmutableTuple<GameConfig, ExperimentSetup> setup,
                                                  SplittableRandom random,
                                                  HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
                                                  TrainableApproximator<DoubleVector> approximator) {
        var experimentSetup = setup.getSecond();
        var rewardAggregator = new DoubleScalarRewardAggregator();
        var clazz = HallwayAction.class;
        var searchNodeMetadataFactory = new PaperMetadataFactory<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(rewardAggregator);
        var paperTreeUpdater = new PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>();
        Supplier<NodeSelector<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> nodeSelector =
            () -> createNodeSelector(experimentSetup.getCpuctParameter(), random.split(), experimentSetup.getGlobalRiskAllowed(), experimentSetup.getSelectorType());

        var strategiesProvider = new StrategiesProvider<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>(
            experimentSetup.getInferenceExistingFlowStrategy(),
            experimentSetup.getInferenceNonExistingFlowStrategy(),
            experimentSetup.getExplorationExistingFlowStrategy(),
            experimentSetup.getExplorationNonExistingFlowStrategy(),
            experimentSetup.getFlowOptimizerType(),
            experimentSetup.getSubTreeRiskCalculatorTypeForKnownFlow(),
            experimentSetup.getSubTreeRiskCalculatorTypeForUnknownFlow(),
            random.split());

        var searchNodeFactory =  new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory);

        var evaluator = resolveEvaluator(setup.getSecond().getEvaluatorType(), random, experimentSetup, rewardAggregator, searchNodeFactory, approximator);

        var paperTrainablePolicySupplier = new TrainablePaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random.split(),
            nodeSelector,
            evaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            experimentSetup.getExplorationConstantSupplier(),
            experimentSetup.getTemperatureSupplier(),
            experimentSetup.getRiskSupplier(),
            strategiesProvider);

        var nnBasedPolicySupplier = new PaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random.split(),
            nodeSelector,
            evaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            strategiesProvider);

        var progressTrackerSettings = new ProgressTrackerSettings(true, true, false, false);

        var trainer = getAbstractTrainer(
            experimentSetup.getTrainerAlgorithm(),
            random.split(),
            hallwayGameInitialInstanceSupplier,
            experimentSetup.getDiscountFactor(),
            evaluator,
            paperTrainablePolicySupplier,
            experimentSetup.getReplayBufferSize(),
            experimentSetup.getMaximalStepCountBound(),
            progressTrackerSettings);

        long trainingTimeInMs = trainPolicy(experimentSetup, trainer);
        this.results = evaluatePolicy(
            random.split(),
            hallwayGameInitialInstanceSupplier,
            experimentSetup,
            Arrays.asList(new PaperBenchmarkingPolicy<>(evaluator.getClass().getName(), nnBasedPolicySupplier)),
            trainingTimeInMs,
            progressTrackerSettings);


        try {
            dumpResults(results, setup);
        } catch (IOException e) {
            logger.error("Results dump failed. ", e);
        }
    }

    private void dumpResults(List<PaperPolicyResults<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> results,
                             ImmutableTuple<GameConfig, ExperimentSetup> setup) throws IOException {

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

        File experimentSetuFile = new File(resultSubfolder, "ExperimentSetup");
        PrintWriter out = new PrintWriter(experimentSetuFile);
        out.print("ExperimentSetup: " + setup.getSecond().toString() + System.lineSeparator() + System.lineSeparator() + "GameSetup: " + setup.getFirst().toString());
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

    private long trainPolicy(ExperimentSetup experimentSetup, AbstractTrainer trainer) {
        long trainingStart = System.currentTimeMillis();
        for (int i = 0; i < experimentSetup.getStageCount(); i++) {
            logger.info("Training policy for [{}]th iteration", i);
            trainer.trainPolicy(experimentSetup.getBatchEpisodeCount());
//            trainer.printDataset();
        }
        return System.currentTimeMillis() - trainingStart;
    }

    private List<PaperPolicyResults<
        HallwayAction,
        DoubleVector,
        EnvironmentProbabilities,
        PaperMetadata<HallwayAction>,
        HallwayStateImpl>>
    evaluatePolicy(
            SplittableRandom random,
            HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
            ExperimentSetup experimentSetup,
            List<PaperBenchmarkingPolicy<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl>> policySupplierList,
            long trainingTimeInMs,
            ProgressTrackerSettings progressTrackerSettings) {
        logger.info("PaperPolicy test starts");
        var benchmark = new PaperBenchmark<>(
            policySupplierList,
            new EnvironmentPolicySupplier(random),
            hallwayGameInitialInstanceSupplier,
            progressTrackerSettings
        );
        long start = System.currentTimeMillis();
        var policyResultList = benchmark.runBenchmark(experimentSetup.getEvalEpisodeCount(), experimentSetup.getMaximalStepCountBound());
        long end = System.currentTimeMillis();
        var benchmarkingTime = end - start;
        logger.info("Benchmarking took [{}] milliseconds", benchmarkingTime);

        for (var policyEntry : policySupplierList) {
            var nnResults = policyResultList
                .stream()
                .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(policyEntry.getPolicyName()))
                .findFirst()
                .get();
            logger.info("[{}]", nnResults.getCalculatedResultStatistics().printToLog());
            logger.info("Training time: [{}]ms", trainingTimeInMs);
            logger.info("Total time: [{}]ms", trainingTimeInMs + nnResults.getBenchmarkingMilliseconds());
        }

        return policyResultList;
    }

    private AbstractTrainer<
        HallwayAction,
        EnvironmentProbabilities,
        PaperMetadata<HallwayAction>,
        HallwayStateImpl>
    getAbstractTrainer(TrainerAlgorithm trainerAlgorithm,
                       SplittableRandom random,
                       HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
                       double discountFactor,
                       PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> nodeEvaluator,
                       TrainablePaperPolicySupplier<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> trainablePaperPolicySupplier,
                       int replayBufferSize,
                       int stepCountLimit,
                       ProgressTrackerSettings progressTrackerSettings) {
        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random.split()),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit,
                    new LinkedList<>(),
                    replayBufferSize,
                    progressTrackerSettings);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random.split()),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    progressTrackerSettings,
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random.split()),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    progressTrackerSettings,
                    stepCountLimit);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);
        }
    }

    private NodeSelector<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> createNodeSelector(
        double cpuctParameter,
        SplittableRandom random,
        double totalRiskAllowed,
        SelectorType selectorType)
    {
        switch (selectorType) {
            case UCB:
                return new PaperNodeSelector<>(cpuctParameter, random.split());
            case VAHY_1:
                return new RiskBasedSelectorVahy<>(cpuctParameter, random.split());
            case LINEAR_HARD_VS_UCB:
                return new RiskBasedSelector<>(cpuctParameter, random.split(), totalRiskAllowed);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }

    private PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> resolveEvaluator(EvaluatorType evaluatorType,
                                                                                                                                         SplittableRandom random,
                                                                                                                                         ExperimentSetup experimentSetup,
                                                                                                                                         DoubleScalarRewardAggregator rewardAggregator,
                                                                                                                                         SearchNodeBaseFactoryImpl<HallwayAction, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction>, HallwayStateImpl> searchNodeFactory,
                                                                                                                                         TrainableApproximator<DoubleVector> approximator) {
        switch (evaluatorType) {
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    EnvironmentProbabilities::getProbabilities,
                    HallwayAction.playerActions,
                    HallwayAction.environmentActions,
                    random.split(),
                    rewardAggregator,
                    experimentSetup.getDiscountFactor());
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
                    random.split(),
                    rewardAggregator,
                    experimentSetup.getDiscountFactor());
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }
}
