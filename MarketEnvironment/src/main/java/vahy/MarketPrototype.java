package vahy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.agent.environment.RealDataMarketPolicySupplier;
import vahy.api.episode.TrainerAlgorithm;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperNodeEvaluator;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.benchmark.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.paperGenerics.reinforcement.learning.AbstractTrainer;
import vahy.paperGenerics.reinforcement.learning.EveryVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.FirstVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.ReplayBufferTrainer;
import vahy.paperGenerics.reinforcement.learning.tf.TFModel;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class MarketPrototype {

    private static final Logger logger = LoggerFactory.getLogger(MarketPrototype.class);

    public static MarketDataProvider createMarketDataProvider(String absoluteFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(absoluteFilePath));
        List<Double> prices = new ArrayList<>();
        List<RealMarketAction> movements = new ArrayList<>();
        lines.forEach(x -> {
                String[] lineParts = x.split(",");
                prices.add(Double.parseDouble(lineParts[0]));
                movements.add(lineParts[1].equals("Up") ? RealMarketAction.MARKET_UP : RealMarketAction.MARKET_DOWN);
            });
        return new MarketDataProvider(movements.toArray(new RealMarketAction[0]), prices.stream().mapToDouble(value -> value).toArray());
    }

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {

        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
        long seed = 0;
        SplittableRandom random = new SplittableRandom(seed);


        // market
        double systemStopLoss = 0.005;
        double constantSpread = 0.0002;
        double priceRange     = 0.0005;
        int tradeSize  = 1;
        int commission = 0; //5 / 1_000_000;

        // environment
        int lookbackLength = 30;

        MarketDataProvider marketDataProvider = createMarketDataProvider("d:/data_for_trading_env_testing/data");
        MarketEnvironmentStaticPart marketEnvironmentStaticPart = new MarketEnvironmentStaticPart(systemStopLoss, constantSpread, priceRange, tradeSize, commission);
        InitialMarketStateSupplier initialMarketStateSupplier = new InitialMarketStateSupplier(random, marketEnvironmentStaticPart, lookbackLength, marketDataProvider);

        // TREE UPDATE POLICY
        TreeUpdateConditionFactory treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(500);

        // MCTS
        double cpuctParameter = 2;

        // MCTS - mc rollout based
        int mcRolloutCount = 1;

        // REINFORCEMENT
        double discountFactor = 1;


        int sampleEpisodeCount = 100;
        int replayBufferSize = 50;
        int stageCount = 200;


        double temperatureSteps = stageCount;
        Supplier<Double> explorationConstantSupplier = () -> 0.3;
        Supplier<Double> temperatureSupplier = () -> 2.0;
        Supplier<Double> riskSupplier = () -> 0.3;

        // NN
        int batchSize = 128;
        // double learningRate = 0.001;
        int trainingEpochCount = 10;

        // risk optimization
        double totalRiskAllowed = 0.3;

        // simmulation after training
        int uniqueEpisodeCount = 1;
        int episodeCount = 1000;
        int stepCountLimit = 1000;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        RewardAggregator rewardAggregator = new DoubleScalarRewardAggregator();
        Class<MarketAction> clazz = MarketAction.class;

        // MCTS WITH NN EVAL
        try(TFModel model = new TFModel(
            initialMarketStateSupplier.createInitialState().getPlayerObservation().getObservedVector().length,
            2 + MarketAction.playerActions.length,
            trainingEpochCount,
            batchSize,
            PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/graph.pb").readAllBytes(),
//            SavedModelBundle.load("C:/Users/Snurka/init_model", "serve"),
            random))
        {
            TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);

            var strategiesProvider = new StrategiesProvider<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState>(
                InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
                InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
                ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
                ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE,
                FlowOptimizerType.SOFT,
                SubTreeRiskCalculatorType.FLOW_SUM,
                SubTreeRiskCalculatorType.PRIOR_SUM,
                random);

            PaperMetadataFactory<MarketAction, DoubleVector, DoubleVector, MarketState> searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);

            Supplier<NodeSelector<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState>> nodeSelectorSupplier = () -> new PaperNodeSelector<>(cpuctParameter, random);

//            PaperNodeSelector<MarketAction, DoubleVector, DoubleVector, MarketState> nodeSelector = new PaperNodeSelector<>(cpuctParameter, random);



            PaperTreeUpdater<MarketAction, DoubleVector, DoubleVector, MarketState> paperTreeUpdater = new PaperTreeUpdater<>();

            PaperNodeEvaluator<MarketAction, DoubleVector, PaperMetadata<MarketAction>, MarketState> marketNodeEvaluator = new PaperNodeEvaluator<>(
                new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory),
                trainableApproximator,
                doubleVector -> {
                    List<MarketAction> arrayList = new ArrayList<>();
                    arrayList.add(MarketAction.UP);
                    arrayList.add(MarketAction.DOWN);
                    List<Double> probabilities = new ArrayList<>();
                    probabilities.add(doubleVector.getObservedVector()[0]);
                    probabilities.add(doubleVector.getObservedVector()[1]);
                    return new ImmutableTuple<>(arrayList, probabilities);
                },
                MarketAction.playerActions,
                MarketAction.environmentActions
                );


            TrainablePaperPolicySupplier<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> paperTrainablePolicySupplier =
                new TrainablePaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelectorSupplier,
                    marketNodeEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory,
                    explorationConstantSupplier,
                    temperatureSupplier,
                    riskSupplier,
                    strategiesProvider);

            PaperPolicySupplier<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> nnBasedPolicySupplier =
                new PaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelectorSupplier,
                    marketNodeEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory,
                    strategiesProvider);

            var progressTrackerSettings = new ProgressTrackerSettings(true, true, false, false);

            AbstractTrainer trainer = getAbstractTrainer(
                TrainerAlgorithm.EVERY_VISIT_MC,
                initialMarketStateSupplier,
                discountFactor,
                marketNodeEvaluator,
                paperTrainablePolicySupplier,
                replayBufferSize,
                stepCountLimit,
                marketDataProvider,
                progressTrackerSettings);


            long trainingStart = System.currentTimeMillis();
            for (int i = 0; i < stageCount; i++) {
                logger.info("Training policy for [{}]th iteration", i);
                trainer.trainPolicy(sampleEpisodeCount);
            }

            long trainingTimeInMs = System.currentTimeMillis() - trainingStart;

            logger.info("PaperPolicy test starts");

            String nnBasedPolicyName = "NNBased";

            PaperBenchmark<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> benchmark = new PaperBenchmark<>(
                Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
                new RealDataMarketPolicySupplier(marketDataProvider),
                initialMarketStateSupplier,
                progressTrackerSettings
            );

            long start = System.currentTimeMillis();
            List<PaperPolicyResults<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState>> policyResultList = benchmark
                .runBenchmark(episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking took [{}] milliseconds", end - start);


            PaperPolicyResults<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> nnResults = policyResultList
                .stream()
                .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName))
                .findFirst()
                .get();

            logger.info("Average reward: [{}]", nnResults.getAverageReward());
            logger.info("Millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
            logger.info("Total expanded nodes: [{}]", marketNodeEvaluator.getNodesExpandedCount());
            logger.info("Kill ratio: [{}]", nnResults.getRiskHitRatio());
            logger.info("Kill counter: [{}]", nnResults.getRiskHitCounter());
            logger.info("Training time: [{}]ms", trainingTimeInMs);

        }

    }

    private static AbstractTrainer<MarketAction, DoubleVector,  PaperMetadata<MarketAction>, MarketState> getAbstractTrainer(
        TrainerAlgorithm trainerAlgorithm,
        InitialMarketStateSupplier initialMarketStateSupplier,
        double discountFactor,
        PaperNodeEvaluator<MarketAction, DoubleVector,  PaperMetadata<MarketAction>, MarketState> nodeEvaluator,
        TrainablePaperPolicySupplier<MarketAction, DoubleVector, DoubleVector, PaperMetadata<MarketAction>, MarketState> trainablePaperPolicySupplier,
        int replayBufferSize,
        int stepCountLimit,
        MarketDataProvider marketDataProvider,
        ProgressTrackerSettings progressTrackerSettings)
    {
        RealDataMarketPolicySupplier environmentSupplier = new RealDataMarketPolicySupplier(marketDataProvider);

        switch(trainerAlgorithm) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer<>(
                    initialMarketStateSupplier,
                    trainablePaperPolicySupplier,
                    environmentSupplier,
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit,
                    new LinkedList<>(),
                    replayBufferSize,
                    progressTrackerSettings);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    initialMarketStateSupplier,
                    trainablePaperPolicySupplier,
                    environmentSupplier,
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    progressTrackerSettings,
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    initialMarketStateSupplier,
                    trainablePaperPolicySupplier,
                    environmentSupplier,
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    progressTrackerSettings,
                    stepCountLimit);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(trainerAlgorithm);

        }
    }
}
