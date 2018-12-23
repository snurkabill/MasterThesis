package vahy;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.agent.environment.RealDataMarketPolicySupplier;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.benchmark.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.paperGenerics.reinforcement.learning.AbstractTrainer;
import vahy.paperGenerics.reinforcement.learning.EveryVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.FirstVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.ReplayBufferTrainer;
import vahy.paperGenerics.reinforcement.learning.TFModel;
import vahy.paperGenerics.reinforcement.learning.Trainer;
import vahy.tempImpl.MarketNodeEvaluator;
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
        cleanUpNativeTempFiles();
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
        double explorationConstant = 0.3;
        double temperature = 2;
        int sampleEpisodeCount = 100;
        int replayBufferSize = 50;
        int stageCountCount = 200;

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

        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        Class<MarketAction> clazz = MarketAction.class;

        // MCTS WITH NN EVAL
        try(TFModel model = new TFModel(
            initialMarketStateSupplier.createInitialState().getObservation().getObservedVector().length,
            2 + MarketAction.playerActions.length,
            trainingEpochCount,
            batchSize,
            PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/graph.pb").readAllBytes(),
            random))
        {
            TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);

            PaperMetadataFactory<MarketAction, DoubleReward, DoubleVector, MarketState> searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);
            PaperNodeSelector<MarketAction, DoubleReward, DoubleVector, MarketState> nodeSelector = new PaperNodeSelector<>(cpuctParameter, random);
            PaperTreeUpdater<MarketAction, DoubleVector, MarketState> paperTreeUpdater = new PaperTreeUpdater<>();
//            PaperNodeEvaluator nnbasedEvaluator = new PaperNodeEvaluator(new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), trainableApproximator);
            MarketNodeEvaluator marketNodeEvaluator = new MarketNodeEvaluator(new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), trainableApproximator);





            TrainablePaperPolicySupplier<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> paperTrainablePolicySupplier =
                new TrainablePaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    marketNodeEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory,
                    explorationConstant,
                    temperature
                );

            PaperPolicySupplier<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> nnBasedPolicySupplier =
                new PaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    marketNodeEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory);

            AbstractTrainer trainer = getAbstractTrainer(
                Trainer.EVERY_VISIT_MC,
                initialMarketStateSupplier,
                discountFactor,
                marketNodeEvaluator,
                paperTrainablePolicySupplier,
                replayBufferSize,
                stepCountLimit,
                marketDataProvider);


            long trainingStart = System.currentTimeMillis();
            for (int i = 0; i < stageCountCount; i++) {
                logger.info("Training policy for [{}]th iteration", i);
                trainer.trainPolicy(sampleEpisodeCount);
            }

            long trainingTimeInMs = System.currentTimeMillis() - trainingStart;

            logger.info("PaperPolicy test starts");

            String nnBasedPolicyName = "NNBased";

            PaperBenchmark<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> benchmark = new PaperBenchmark<>(
                Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
                new RealDataMarketPolicySupplier(marketDataProvider),
                initialMarketStateSupplier
            );

            long start = System.currentTimeMillis();
            List<PaperPolicyResults<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState>> policyResultList = benchmark
                .runBenchmark(uniqueEpisodeCount, episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking took [{}] milliseconds", end - start);


            PaperPolicyResults<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> nnResults = policyResultList
                .stream()
                .filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName))
                .findFirst()
                .get();

            logger.info("NN Based Average reward: [{}]", nnResults.getAverageReward());
            logger.info("NN Based millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
            logger.info("NN Based total expanded nodes: [{}]", marketNodeEvaluator.getNodesExpandedCount());
            logger.info("NN Based kill ratio: [{}]", nnResults.getRiskHitRatio());
            logger.info("NN Based kill counter: [{}]", nnResults.getRiskHitCounter());
            logger.info("NN Based training time: [{}]ms", trainingTimeInMs);

        }

    }

    private static AbstractTrainer<MarketAction, PaperMetadata<MarketAction, DoubleReward>, MarketState> getAbstractTrainer(
        Trainer trainer,
        InitialMarketStateSupplier initialMarketStateSupplier,
        double discountFactor,
        MarketNodeEvaluator nodeEvaluator,
        TrainablePaperPolicySupplier<MarketAction, DoubleReward, DoubleVector, PaperMetadata<MarketAction, DoubleReward>, MarketState> trainablePaperPolicySupplier,
        int replayBufferSize,
        int stepCountLimit,
        MarketDataProvider marketDataProvider)
    {
        RealDataMarketPolicySupplier environmentSupplier = new RealDataMarketPolicySupplier(marketDataProvider);

        switch(trainer) {
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
                    replayBufferSize);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer<>(
                    initialMarketStateSupplier,
                    trainablePaperPolicySupplier,
                    environmentSupplier,
                    nodeEvaluator,
                    discountFactor,
                    new DoubleScalarRewardAggregator(),
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer<>(
                    initialMarketStateSupplier,
                    trainablePaperPolicySupplier,
                    environmentSupplier,
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
