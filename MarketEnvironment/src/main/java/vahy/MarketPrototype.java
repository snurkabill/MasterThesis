package vahy;

import vahy.api.learning.AbstractTrainer;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.environment.HallwayAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.RealMarketAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.utils.EnumUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;

public class MarketPrototype {

    public static MarketDataProvider createMarketDataProvider(String absoluteFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(absoluteFilePath));
        List<Double> prices = new ArrayList<>();
        List<RealMarketAction> movements = new ArrayList<>();
        lines.forEach(x -> {
                String[] lineParts = x.split(",");
                prices.add(Double.parseDouble(lineParts[0]));
                movements.add(lineParts[1].equals("UP") ? RealMarketAction.MARKET_UP : RealMarketAction.MARKET_DOWN);
            });
        return new MarketDataProvider(movements.toArray(new RealMarketAction[0]), prices.stream().mapToDouble(value -> value).toArray());
    }

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        cleanUpNativeTempFiles();
        long seed = 0;
        SplittableRandom random = new SplittableRandom(seed);


        // market
        double systemStopLoss = 0.005;
        double constantSpread = 0.0002;
        double priceRange     = 0.0005;
        int tradeSize  = 1;
        int commission = 5 / 1_000_000;

        // environment
        int lookbackLength = 30;

        MarketDataProvider marketDataProvider = createMarketDataProvider("d:/data_for_trading_enf_testing/data");
        MarketEnvironmentStaticPart marketEnvironmentStaticPart = new MarketEnvironmentStaticPart(systemStopLoss, constantSpread, priceRange, tradeSize, commission);
        InitialMarketStateSupplier initialMarketStateSupplier = new InitialMarketStateSupplier(random, marketEnvironmentStaticPart, lookbackLength, marketDataProvider);

        // TREE UPDATE POLICY
        TreeUpdateConditionFactory treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(100);

        // MCTS
        double cpuctParameter = 2;

        // MCTS - mc rollout based
        int mcRolloutCount = 1;

        // REINFORCEMENT
        double discountFactor = 1;
        double explorationConstant = 0.3;
        double temperature = 2;
        int sampleEpisodeCount = 10;
        int replayBufferSize = 50;
        int stageCountCount = 200;

        // NN
        int batchSize = 4;
        // double learningRate = 0.001;
        int trainingEpochCount = 300;

        // risk optimization
        boolean optimizeFlowInSearchTree = true;
        double totalRiskAllowed = 0.02;

        // simmulation after training
        int uniqueEpisodeCount = 1;
        int episodeCount = 1000;
        int stepCountLimit = 1000;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        Class<HallwayAction> clazz = HallwayAction.class;

        // MCTS WITH NN EVAL
        try(TFModel model = new TFModel(
            hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
            NodeEvaluator.POLICY_START_INDEX + HallwayAction.playerActions.length,
            trainingEpochCount,
            batchSize,
            PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/graph.pb").readAllBytes(),
            random))
        {
            TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);

            PaperMetadataFactory<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);
            PaperNodeSelector<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl> nodeSelector = new PaperNodeSelector<>(cpuctParameter, random);
            PaperTreeUpdater<HallwayAction, DoubleVector, HallwayStateImpl> paperTreeUpdater = new PaperTreeUpdater<>();
            PaperNodeEvaluator nnbasedEvaluator = new PaperNodeEvaluator(new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), trainableApproximator);


            TrainablePaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> paperTrainablePolicySupplier =
                new TrainablePaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    nnbasedEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory,
                    explorationConstant,
                    temperature
                );

            PaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnBasedPolicySupplier =
                new PaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    nnbasedEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory);

            AbstractTrainer trainer = getAbstractTrainer(Trainer.EVERY_VISIT_MC,
                random,
                hallwayGameInitialInstanceSupplier,
                discountFactor,
                nnbasedEvaluator,
                paperTrainablePolicySupplier,
                replayBufferSize,
                stepCountLimit);


            long trainingStart = System.currentTimeMillis();
            for (int i = 0; i < stageCountCount; i++) {
                logger.info("Training policy for [{}]th iteration", i);
                trainer.trainPolicy(sampleEpisodeCount);
            }

            long trainingTimeInMs = System.currentTimeMillis() - trainingStart;

            logger.info("PaperPolicy test starts");

            String nnBasedPolicyName = "NNBased";


            PaperBenchmark<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> benchmark = new PaperBenchmark<>(
                Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
                new EnvironmentPolicySupplier(random),
                hallwayGameInitialInstanceSupplier
            );

            long start = System.currentTimeMillis();
            List<PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>> policyResultList = benchmark
                .runBenchmark(uniqueEpisodeCount, episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking took [{}] milliseconds", end - start);


            PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnResults = policyResultList
                .stream()
                .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName))
                .findFirst()
                .get();

            logger.info("NN Based Average reward: [{}]", nnResults.getAverageReward());
            logger.info("NN Based millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
            logger.info("NN Based total expanded nodes: [{}]", nnbasedEvaluator.getNodesExpandedCount());
            logger.info("NN Based kill ratio: [{}]", nnResults.getRiskHitRatio());
            logger.info("NN Based kill counter: [{}]", nnResults.getRiskHitCounter());
            logger.info("NN Based training time: [{}]ms", trainingTimeInMs);

        }

    }

    private static AbstractTrainer<
        HallwayAction,
        PaperMetadata<HallwayAction, DoubleReward>,
        HallwayStateImpl>
    getAbstractTrainer(Trainer trainer,
                       SplittableRandom random,
                       HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
                       double discountFactor,
                       PaperNodeEvaluator nodeEvaluator,
                       TrainablePaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> trainablePaperPolicySupplier,
                       int replayBufferSize,
                       int stepCountLimit) {
        switch(trainer) {
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
                throw EnumUtils.createExceptionForUnknownEnumValue(trainer);

        }

    }


    private static void cleanUpNativeTempFiles() {
        // TODO: code duplicity
        System.gc();
        String bridJFolderNameStart = "BridJExtractedLibraries";
        String CLPFolderNameStart = "CLPExtractedLib";
        String TFFolderNameStart = "tensorflow_native_libraries";
        String tempPath = System.getProperty("java.io.tmpdir");
        File file = new File(tempPath);
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
        if (directories != null) {
            Arrays.stream(directories).filter(x -> x.startsWith(bridJFolderNameStart) || x.startsWith(CLPFolderNameStart) || x.startsWith(TFFolderNameStart)).forEach(x -> {
                try {
                    FileUtils.deleteDirectory(new File(tempPath + "/" + x));
                } catch (IOException e) {
                    e.printStackTrace(); // todo: deal with this later
                }
            });
        }

    }

}
