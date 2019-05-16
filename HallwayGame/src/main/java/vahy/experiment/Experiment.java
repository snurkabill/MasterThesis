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
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperNodeEvaluator;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.benchmark.PaperPolicyResults;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;

public class Experiment {

    private final Logger logger = LoggerFactory.getLogger(Experiment.class);

    private List<PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>> results;

    public List<PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>> getResults() {
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
                createPolicyAndRunProcess(setup, random, hallwayGameInitialInstanceSupplier, new EmptyApproximator<>());
                break;
            case HASHMAP:
                createPolicyAndRunProcess(setup, random, hallwayGameInitialInstanceSupplier, new DataTableApproximator<>(HallwayAction.playerActions.length, setup.getSecond().omitProbabilities()));
                break;
            case HASHMAP_LR:
                createPolicyAndRunProcess(setup, random, hallwayGameInitialInstanceSupplier, new DataTableApproximatorWithLr<>(HallwayAction.playerActions.length, setup.getSecond().getLearningRate(), setup.getSecond().omitProbabilities()));
                break;
            case NN:
            {
                try(TFModel model = new TFModel(
                    inputLenght,
                    PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
                    setup.getSecond().getTrainingEpochCount(),
                    setup.getSecond().getTrainingBatchSize(),
                    PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/graph_" + setup.getSecond().getHallwayInstance().toString() + ".pb").readAllBytes(),
                    random,
                    setup.getSecond().omitProbabilities())
                ) //            SavedModelBundle.load("C:/Users/Snurka/init_model", "serve"),
                {
                    TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);
                    createPolicyAndRunProcess(setup, random, hallwayGameInitialInstanceSupplier, trainableApproximator);
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
        var searchNodeMetadataFactory = new PaperMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(rewardAggregator);
        var paperTreeUpdater = new PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>();
        var nodeSelector = createNodeSelector(experimentSetup.getCpuctParameter(), random, experimentSetup.getGlobalRiskAllowed(), experimentSetup.getSelectorType());

        var strategiesProvider = new StrategiesProvider<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>(
            experimentSetup.getInferenceExistingFlowStrategy(),
            experimentSetup.getInferenceNonExistingFlowStrategy(),
            experimentSetup.getExplorationExistingFlowStrategy(),
            experimentSetup.getExplorationNonExistingFlowStrategy(),
            experimentSetup.getFlowOptimizerType(),
            experimentSetup.getSubTreeRiskCalculatorTypeForKnownFlow(),
            experimentSetup.getSubTreeRiskCalculatorTypeForUnknownFlow(),
            random);

        var nnbasedEvaluator = new PaperNodeEvaluator<>(
            new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
            approximator,
            EnvironmentProbabilities::getProbabilities,
            HallwayAction.playerActions, HallwayAction.environmentActions);

        var paperTrainablePolicySupplier = new TrainablePaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random,
            nodeSelector,
            nnbasedEvaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            experimentSetup.getExplorationConstantSupplier(),
            experimentSetup.getTemperatureSupplier(),
            strategiesProvider
        );

        var nnBasedPolicySupplier = new PaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random,
            nodeSelector,
            nnbasedEvaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            strategiesProvider);

        var trainer = getAbstractTrainer(
            experimentSetup.getTrainerAlgorithm(),
            random,
            hallwayGameInitialInstanceSupplier,
            experimentSetup.getDiscountFactor(),
            nnbasedEvaluator,
            paperTrainablePolicySupplier,
            experimentSetup.getReplayBufferSize(),
            experimentSetup.getMaximalStepCountBound());

        long trainingTimeInMs = trainPolicy(experimentSetup, trainer);
        this.results = evaluatePolicy(random, hallwayGameInitialInstanceSupplier, experimentSetup, nnbasedEvaluator, nnBasedPolicySupplier, trainingTimeInMs);
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
        DoubleReward,
        DoubleVector,
        EnvironmentProbabilities,
        PaperMetadata<HallwayAction, DoubleReward>,
        HallwayStateImpl>>
    evaluatePolicy(
            SplittableRandom random,
            HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
            ExperimentSetup experimentSetup,
            PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnbasedEvaluator,
            PaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnBasedPolicySupplier,
            long trainingTimeInMs) {
        logger.info("PaperPolicy test starts");
        String nnBasedPolicyName = "NNBased";
        var benchmark = new PaperBenchmark<>(
            Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
            new EnvironmentPolicySupplier(random),
            hallwayGameInitialInstanceSupplier
        );
        long start = System.currentTimeMillis();
        var policyResultList = benchmark.runBenchmark(experimentSetup.getEvalEpisodeCount(), experimentSetup.getMaximalStepCountBound());
        long end = System.currentTimeMillis();
        logger.info("Benchmarking took [{}] milliseconds", end - start);

        var nnResults = policyResultList
            .stream()
            .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName))
            .findFirst()
            .get();
        logger.info("Average reward: [{}]", nnResults.getAverageReward());
        logger.info("Millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
        logger.info("Total expanded nodes: [{}]", nnbasedEvaluator.getNodesExpandedCount());
        logger.info("Kill ratio: [{}]", nnResults.getRiskHitRatio());
        logger.info("Kill counter: [{}]", nnResults.getRiskHitCounter());
        logger.info("Training time: [{}]ms", trainingTimeInMs);

        return policyResultList;
    }

    private AbstractTrainer<
        HallwayAction,
        EnvironmentProbabilities,
        PaperMetadata<HallwayAction, DoubleReward>,
        HallwayStateImpl>
    getAbstractTrainer(TrainerAlgorithm trainerAlgorithm,
                       SplittableRandom random,
                       HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
                       double discountFactor,
                       PaperNodeEvaluator<HallwayAction, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nodeEvaluator,
                       TrainablePaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> trainablePaperPolicySupplier,
                       int replayBufferSize,
                       int stepCountLimit) {
        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit,
                    new LinkedList<>(),
                    replayBufferSize);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    hallwayGameInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);
        }
    }

    private NodeSelector<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> createNodeSelector(
        double cpuctParameter,
        SplittableRandom random,
        double totalRiskAllowed,
        SelectorType selectorType)
    {
        switch (selectorType) {
            case UCB:
                return new PaperNodeSelector<>(cpuctParameter, random);
            case VAHY_1:
                return new RiskBasedSelectorVahy<>(cpuctParameter, random);
            case LINEAR_HARD_VS_UCB:
                return new RiskBasedSelector<>(cpuctParameter, random, totalRiskAllowed);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(selectorType);
        }
    }
}
