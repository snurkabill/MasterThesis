package vahy.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RandomWalkExample;
import vahy.api.episode.TrainerAlgorithm;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.environment.RandomWalkAction;
import vahy.environment.RandomWalkInitialInstanceSupplier;
import vahy.environment.RandomWalkProbabilities;
import vahy.environment.RandomWalkSetup;
import vahy.environment.RandomWalkState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.opponent.RandomWalkOpponentSupplier;
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
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class Experiment {

    // TODO: REMOVE CODE REDUNDANCY

    private final Logger logger = LoggerFactory.getLogger(Experiment.class);

    private List<PaperPolicyResults<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState>> results;

    public List<PaperPolicyResults<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState>> getResults() {
        return results;
    }

    public void prepareAndRun(ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup, SplittableRandom random) throws IOException {
        var provider = new RandomWalkInitialInstanceSupplier(setup.getFirst());
        var inputLength = provider.createInitialState().getPlayerObservation().getObservedVector().length;

        switch (setup.getSecond().getApproximatorType()) {
            case EMPTY:
                createPolicyAndRunProcess(setup, random, provider, new EmptyApproximator<>());
                break;
            case HASHMAP:
                createPolicyAndRunProcess(setup, random, provider, new DataTableApproximator<>(RandomWalkAction.playerActions.length, setup.getSecond().omitProbabilities()));
                break;
            case HASHMAP_LR:
                createPolicyAndRunProcess(setup, random, provider, new DataTableApproximatorWithLr<>(RandomWalkAction.playerActions.length, setup.getSecond().getLearningRate(), setup.getSecond().omitProbabilities()));
                break;
            case NN:
            {
                try(TFModel model = new TFModel(
                    inputLength,
                    PaperModel.POLICY_START_INDEX + RandomWalkAction.playerActions.length,
                    setup.getSecond().getTrainingEpochCount(),
                    setup.getSecond().getTrainingBatchSize(),
                    RandomWalkExample.class.getClassLoader().getResourceAsStream("tfModel/graph_randomWalk.pb").readAllBytes(),
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
                                           TrainableApproximator<DoubleVector> approximator) {
        var experimentSetup = setup.getSecond();
        var rewardAggregator = new DoubleScalarRewardAggregator();
        var clazz = RandomWalkAction.class;
        var searchNodeMetadataFactory = new PaperMetadataFactory<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, RandomWalkState>(rewardAggregator);
        var paperTreeUpdater = new PaperTreeUpdater<RandomWalkAction, DoubleVector, RandomWalkProbabilities, RandomWalkState>();
        var nodeSelector = new PaperNodeSelector<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, RandomWalkState>(setup.getSecond().getCpuctParameter(), random);

        Supplier<NodeSelector<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState>> nodeSelectorSupplier =
            () -> new PaperNodeSelector<>(setup.getSecond().getCpuctParameter(), random);

        var nnbasedEvaluator = new PaperNodeEvaluator<>(
            new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
            approximator,
            RandomWalkProbabilities::getProbabilities,
            RandomWalkAction.playerActions, RandomWalkAction.environmentActions);

        var strategiesProvider = new StrategiesProvider<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState>(
            experimentSetup.getInferenceExistingFlowStrategy(),
            experimentSetup.getInferenceNonExistingFlowStrategy(),
            experimentSetup.getExplorationExistingFlowStrategy(),
            experimentSetup.getExplorationNonExistingFlowStrategy(),
            experimentSetup.getFlowOptimizerType(),
            experimentSetup.getSubTreeRiskCalculatorTypeForKnownFlow(),
            experimentSetup.getSubTreeRiskCalculatorTypeForUnknownFlow(),
            random);

        var paperTrainablePolicySupplier = new TrainablePaperPolicySupplier<>(
            clazz,
            searchNodeMetadataFactory,
            experimentSetup.getGlobalRiskAllowed(),
            random,
            nodeSelectorSupplier,
            nnbasedEvaluator,
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
            nnbasedEvaluator,
            paperTreeUpdater,
            experimentSetup.getTreeUpdateConditionFactory(),
            strategiesProvider);

        var trainer = getAbstractTrainer(
            experimentSetup.getTrainerAlgorithm(),
            random,
            RandomWalkGameInitialInstanceSupplier,
            experimentSetup.getDiscountFactor(),
            nnbasedEvaluator,
            paperTrainablePolicySupplier,
            experimentSetup.getReplayBufferSize(),
            experimentSetup.getMaximalStepCountBound());

        long trainingTimeInMs = trainPolicy(experimentSetup, trainer);
        this.results = evaluatePolicy(random, RandomWalkGameInitialInstanceSupplier, experimentSetup, nnbasedEvaluator, nnBasedPolicySupplier, trainingTimeInMs);
    }

    private long trainPolicy(ExperimentSetup experimentSetup, AbstractTrainer trainer) {
        long trainingStart = System.currentTimeMillis();
        for (int i = 0; i < experimentSetup.getStageCount(); i++) {
            logger.info("Training policy for [{}]th iteration", i);
            trainer.trainPolicy(experimentSetup.getBatchEpisodeCount());
            trainer.printDataset();
        }
        return System.currentTimeMillis() - trainingStart;
    }

    private List<PaperPolicyResults<
        RandomWalkAction,
        DoubleReward,
        DoubleVector,
        RandomWalkProbabilities,
        PaperMetadata<RandomWalkAction, DoubleReward>,
        RandomWalkState>>
    evaluatePolicy(
        SplittableRandom random,
        RandomWalkInitialInstanceSupplier randomWalkInitialInstanceSupplier,
        ExperimentSetup experimentSetup,
        PaperNodeEvaluator<RandomWalkAction, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState> nnbasedEvaluator,
        PaperPolicySupplier<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState> nnBasedPolicySupplier,
        long trainingTimeInMs) {
        logger.info("PaperPolicy test starts");
        String nnBasedPolicyName = "NNBased";
        var benchmark = new PaperBenchmark<>(
            Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
            new RandomWalkOpponentSupplier(random),
            randomWalkInitialInstanceSupplier
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
        RandomWalkAction,
        RandomWalkProbabilities,
        PaperMetadata<RandomWalkAction, DoubleReward>,
        RandomWalkState>
    getAbstractTrainer(TrainerAlgorithm trainerAlgorithm,
                       SplittableRandom random,
                       RandomWalkInitialInstanceSupplier randomWalkInitialInstanceSupplier,
                       double discountFactor,
                       PaperNodeEvaluator<RandomWalkAction, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState> nodeEvaluator,
                       TrainablePaperPolicySupplier<RandomWalkAction, DoubleReward, DoubleVector, RandomWalkProbabilities, PaperMetadata<RandomWalkAction, DoubleReward>, RandomWalkState> trainablePaperPolicySupplier,
                       int replayBufferSize,
                       int stepCountLimit) {
        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    randomWalkInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new RandomWalkOpponentSupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit,
                    new LinkedList<>(),
                    replayBufferSize);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    randomWalkInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new RandomWalkOpponentSupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    randomWalkInitialInstanceSupplier,
                    trainablePaperPolicySupplier,
                    new RandomWalkOpponentSupplier(random),
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);
        }
    }

}
