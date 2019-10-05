package vahy.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.Analyzer;
import vahy.RandomWalkExample;
import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.config.EvaluatorType;
import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkInitialInstanceSupplier;
import vahy.environment.RandomWalkProbabilities;
import vahy.environment.RandomWalkSetup;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.opponent.RandomWalkOpponentSupplier;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.evaluator.MonteCarloNodeEvaluator;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.evaluator.RamcpNodeEvaluator;
import vahy.paperGenerics.experiment.PaperPolicyResults;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.impl.predictor.DataTablePredictor;
import vahy.paperGenerics.reinforcement.DataTablePredictorWithLr;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.predictor.TrainableApproximator;
import vahy.api.predictor.TrainablePredictor;
import vahy.paperGenerics.reinforcement.episode.sampler.PaperRolloutGameSampler;
import vahy.paperGenerics.reinforcement.learning.AbstractTrainer;
import vahy.paperGenerics.reinforcement.learning.EveryVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.FirstVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.ReplayBufferTrainer;
import vahy.paperGenerics.reinforcement.learning.tf.TFModel;
import vahy.paperGenerics.selector.PaperNodeSelector;
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

    // TODO: REMOVE CODE REDUNDANCY
    // TODO: this file is really shitty.

    private final Logger logger = LoggerFactory.getLogger(Experiment.class);

    private List<PaperPolicyResults<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState>> results;

    public List<PaperPolicyResults<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState>> getResults() {
        return results;
    }

    public void prepareAndRun(ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup, SplittableRandom random) throws IOException {
        var provider = new RandomWalkInitialInstanceSupplier(setup.getFirst());
        var inputLength = provider.createInitialState().getPlayerObservation().getObservedVector().length;

        var actionCount = RandomWalkAction.playerActions.length;
        var defaultPrediction = new double[2 + actionCount];
        defaultPrediction[0] = 0;
        defaultPrediction[1] = 0.0;
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i + 2] = 1.0 / actionCount;
        }

        switch (setup.getSecond().getApproximatorType()) {
            case EMPTY:
                createPolicyAndRunProcess(setup, random, provider, new EmptyPredictor<>(defaultPrediction));
                break;
            case HASHMAP:
                createPolicyAndRunProcess(setup, random, provider, new DataTablePredictor<>(defaultPrediction));
                break;
            case HASHMAP_LR:
                createPolicyAndRunProcess(setup, random, provider, new DataTablePredictorWithLr<>(defaultPrediction, setup.getSecond().getLearningRate(), RandomWalkAction.playerActions.length));
                break;
            case TF_NN:
            {
                try(TFModel model = new TFModel(
                    inputLength,
                    PaperModel.POLICY_START_INDEX + RandomWalkAction.playerActions.length,
                    setup.getSecond().getTrainingEpochCount(),
                    setup.getSecond().getTrainingBatchSize(),
                    RandomWalkExample.class.getClassLoader().getResourceAsStream("tfModel/graph_RANDOM_WALK_LINEAR.pb").readAllBytes(),
                    random)
                )
                {
                    TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);
                    createPolicyAndRunProcess(setup, random, provider, trainableApproximator);
                }
            }
            break;
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(setup.getSecond().getApproximatorType());
        }
    }

    private void createPolicyAndRunProcess(ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup,
                                           SplittableRandom random,
                                           RandomWalkInitialInstanceSupplier RandomWalkGameInitialInstanceSupplier,
                                           TrainablePredictor<DoubleVector> predictor) {
        var experimentSetup = setup.getSecond();
        var rewardAggregator = new DoubleScalarRewardAggregator();
        var clazz = RandomWalkAction.class;
        var searchNodeMetadataFactory = new PaperMetadataFactory<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState>(rewardAggregator);
        var paperTreeUpdater = new PaperTreeUpdater<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState>();
        var nodeSelector = new PaperNodeSelector<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState>(setup.getSecond().getCpuctParameter(), random);

        Supplier<NodeSelector<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState>> nodeSelectorSupplier =
            () -> new PaperNodeSelector<>(setup.getSecond().getCpuctParameter(), random);


        var evaluator = resolveEvaluator(setup.getSecond().getEvaluatorType(), random.split(), setup.getSecond(), rewardAggregator, new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), predictor);
//
//        var evaluator = new PaperNodeEvaluator<>(
//            new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
//            predictor,
//            RandomWalkProbabilities::getProbabilities,
//            RandomWalkAction.playerActions, RandomWalkAction.environmentActions);
//
//
////        var evaluator = new RamcpNodeEvaluator<>(
////            new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
////            RandomWalkProbabilities::getProbabilities,
////            RandomWalkAction.playerActions,
////            RandomWalkAction.environmentActions,
////            random.split(),
////            rewardAggregator,
////            experimentSetup.getDiscountFactor());

        var strategiesProvider = new StrategiesProvider<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState>(
            experimentSetup.getInferenceExistingFlowStrategy(),
            experimentSetup.getInferenceNonExistingFlowStrategy(),
            experimentSetup.getExplorationExistingFlowStrategy(),
            experimentSetup.getExplorationNonExistingFlowStrategy(),
            experimentSetup.getFlowOptimizerType(),
            experimentSetup.getSubTreeRiskCalculatorTypeForKnownFlow(),
            experimentSetup.getSubTreeRiskCalculatorTypeForUnknownFlow());

        var paperTrainablePolicySupplier = new TrainablePaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random,
            nodeSelectorSupplier,
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
            random,
            nodeSelectorSupplier,
            evaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            strategiesProvider);

        var progressTrackerSettings = new ProgressTrackerSettings(true, false, false, false);

        var trainer = getAbstractTrainer(
            experimentSetup.getTrainerAlgorithm(),
            random,
            RandomWalkGameInitialInstanceSupplier,
            experimentSetup.getDiscountFactor(),
            predictor,
            paperTrainablePolicySupplier,
            experimentSetup.getReplayBufferSize(),
            experimentSetup.getMaximalStepCountBound(),
            progressTrackerSettings);

        long trainingTimeInMs = trainPolicy(experimentSetup, trainer);
        this.results = evaluatePolicy(random, RandomWalkGameInitialInstanceSupplier, experimentSetup, evaluator, nnBasedPolicySupplier, progressTrackerSettings, trainingTimeInMs);
        Analyzer.printStatistics(results.get(0).getRewardAndRiskList());

        try {
            dumpResults(results, setup);
        } catch (IOException e) {
            logger.error("Results dump failed. ", e);
        }
    }

    private void dumpResults(List<PaperPolicyResults<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState>> results,
                             ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup) throws IOException {

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
        RandomWalkAction,
        DoubleVector,
        RandomWalkProbabilities,
        PaperMetadata<RandomWalkAction>,
        RandomWalkState>>
    evaluatePolicy(
        SplittableRandom random,
        RandomWalkInitialInstanceSupplier randomWalkInitialInstanceSupplier,
        ExperimentSetup experimentSetup,
        PaperNodeEvaluator<RandomWalkAction, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> nnbasedEvaluator,
        PaperPolicySupplier<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> nnBasedPolicySupplier,
        ProgressTrackerSettings progressTrackerSettings,
        long trainingTimeInMs) {
        logger.info("PaperPolicy test starts");
        String nnBasedPolicyName = "NNBased";

        var benchmark = new PaperBenchmark<>(
            Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
            new RandomWalkOpponentSupplier(random),
            randomWalkInitialInstanceSupplier,
            progressTrackerSettings
        );
        long start = System.currentTimeMillis();
        var policyResultList = benchmark.runBenchmark(experimentSetup.getEvalEpisodeCount(), experimentSetup.getMaximalStepCountBound(), 1);
        long end = System.currentTimeMillis();
        logger.info("Benchmarking took [{}] milliseconds", end - start);

        var nnResults = policyResultList
            .stream()
            .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName))
            .findFirst()
            .get();
        logger.info("[{}]", nnResults.getCalculatedResultStatistics().printToLog());
        logger.info("Training time: [{}]ms", trainingTimeInMs);
        logger.info("Total time: [{}]ms", trainingTimeInMs + nnResults.getBenchmarkingMilliseconds());

        return policyResultList;
    }

    private AbstractTrainer<
        RandomWalkAction,
        RandomWalkProbabilities,
        PaperMetadata<RandomWalkAction>,
        RandomWalkState>
    getAbstractTrainer(TrainerAlgorithm trainerAlgorithm,
                       SplittableRandom random,
                       RandomWalkInitialInstanceSupplier randomWalkInitialInstanceSupplier,
                       double discountFactor,
                       TrainablePredictor<DoubleVector> predictor,
                       TrainablePaperPolicySupplier<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> trainablePaperPolicySupplier,
                       int replayBufferSize,
                       int stepCountLimit,
                       ProgressTrackerSettings progressTrackerSettings) {

        var gameSampler = new PaperRolloutGameSampler<>(
            randomWalkInitialInstanceSupplier,
            trainablePaperPolicySupplier,
            new RandomWalkOpponentSupplier(random.split()),
            progressTrackerSettings,
            stepCountLimit,
            1);


        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    gameSampler,
                    predictor,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    new LinkedList<>(),
                    replayBufferSize);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    gameSampler,
                    predictor,
                    discountFactor,
                    new DoubleScalarRewardAggregator());
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    gameSampler,
                    predictor,
                    discountFactor,
                    new DoubleScalarRewardAggregator());
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);
        }
    }

    private PaperNodeEvaluator<RandomWalkAction, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> resolveEvaluator(EvaluatorType evaluatorType,
                                                                                                                                             SplittableRandom random,
                                                                                                                                             ExperimentSetup experimentSetup,
                                                                                                                                             DoubleScalarRewardAggregator rewardAggregator,
                                                                                                                                             SearchNodeBaseFactoryImpl<RandomWalkAction, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction>, RandomWalkState> searchNodeFactory,
                                                                                                                                             TrainablePredictor<DoubleVector> predictor) {
        switch (evaluatorType) {
            case MONTE_CARLO:
                return new MonteCarloNodeEvaluator<>(
                    searchNodeFactory,
                    RandomWalkProbabilities::getProbabilities,
                    RandomWalkAction.playerActions,
                    RandomWalkAction.environmentActions,
                    random.split(),
                    rewardAggregator,
                    experimentSetup.getDiscountFactor());
            case RALF:
                return new PaperNodeEvaluator<>(
                    searchNodeFactory,
                    predictor,
                    RandomWalkProbabilities::getProbabilities,
                    RandomWalkAction.playerActions,
                    RandomWalkAction.environmentActions);
            case RAMCP:
                return new RamcpNodeEvaluator<>(
                    searchNodeFactory,
                    RandomWalkProbabilities::getProbabilities,
                    RandomWalkAction.playerActions,
                    RandomWalkAction.environmentActions,
                    random.split(),
                    rewardAggregator,
                    experimentSetup.getDiscountFactor());
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(evaluatorType);
        }
    }

}
