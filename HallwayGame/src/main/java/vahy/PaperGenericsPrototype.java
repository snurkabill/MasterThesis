package vahy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.TrainerAlgorithm;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.environment.HallwayAction;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.environment.state.StateRepresentation;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperMetadataFactory;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperNodeEvaluatorWORKINGCOPY;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperBenchmark;
import vahy.paperGenerics.benchmark.PaperBenchmarkingPolicy;
import vahy.paperGenerics.benchmark.PaperPolicyResults;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.policy.environment.EnvironmentPolicySupplier;
import vahy.paperGenerics.reinforcement.TrainableApproximator;
import vahy.paperGenerics.reinforcement.learning.AbstractTrainer;
import vahy.paperGenerics.reinforcement.learning.EveryVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.FirstVisitMonteCarloTrainer;
import vahy.paperGenerics.reinforcement.learning.ReplayBufferTrainer;
import vahy.paperGenerics.reinforcement.learning.tf.TFModel;
import vahy.utils.EnumUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperGenericsPrototype {

    private static final Logger logger = LoggerFactory.getLogger(PaperGenericsPrototype.class);

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        cleanUpNativeTempFiles();
        long seed = 2;
        SplittableRandom random = new SplittableRandom(seed);
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.FULL)
            .buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = getHallwayGameInitialInstanceSupplier(random, gameConfig);

        // TREE UPDATE POLICY
        TreeUpdateConditionFactory treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(100);

        // MCTS
        double cpuctParameter = 2;

        // MCTS - mc rollout based
        int mcRolloutCount = 1;

        // REINFORCEMENT
        double discountFactor = 1;
        double explorationConstant = 0.3;

        int sampleEpisodeCount = 10;
        int replayBufferSize = 100;
        int stageCount = 200;

        Supplier<Double> explorationConstantSupplier = new Supplier<>() {

            private int callCount = 0;

            @Override
            public Double get() {
                // callCount++;
                // return Math.exp(-callCount / 500.0);
                return 0.3;
            }
        };;
        Supplier<Double> temperatureSupplier = new Supplier<>() {

            private int callCount = 0;

            @Override
            public Double get() {
                callCount++;
                // return Math.exp(-callCount / 5000.0) * 3;
                return 2.0;
            }
        };


        // NN
        int batchSize = 4;
        // double learningRate = 0.001;
        int trainingEpochCount = 300;


        // risk optimization
        boolean optimizeFlowInSearchTree = true;
        double totalRiskAllowed = 0.02;

        // simmulation after training
        int uniqueEpisodeCount = 1;
        int episodeCount = 10000;
        int stepCountLimit = 1000;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        Class<HallwayAction> clazz = HallwayAction.class;

        // MCTS WITH NN EVAL
        try(TFModel model = new TFModel(
            hallwayGameInitialInstanceSupplier.createInitialState().getPlayerObservation().getObservedVector().length,
            PaperModel.POLICY_START_INDEX + HallwayAction.playerActions.length,
            trainingEpochCount,
            batchSize,

             PaperGenericsPrototype.class.getClassLoader().getResourceAsStream("tfModel/rgraph.pb").readAllBytes(),
//            SavedModelBundle.load("C:/Users/Snurka/init_model", "serve"),
            random))
        {
            TrainableApproximator<DoubleVector> trainableApproximator = new TrainableApproximator<>(model);

            PaperMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);
            PaperNodeSelector<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> nodeSelector = new PaperNodeSelector<>(cpuctParameter, random);
            PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> paperTreeUpdater = new PaperTreeUpdater<>();
//            PaperNodeEvaluator nnbasedEvaluator = new PaperNodeEvaluator(new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), trainableApproximator, allPlayerActions, allOpponentActions);
            PaperNodeEvaluatorWORKINGCOPY nnbasedEvaluator = new PaperNodeEvaluatorWORKINGCOPY(new SearchNodeBaseFactoryImpl<>(searchNodeMetadataFactory), trainableApproximator);


            TrainablePaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> paperTrainablePolicySupplier =
                new TrainablePaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    nnbasedEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory,
                    explorationConstantSupplier,
                    temperatureSupplier
                );

            PaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnBasedPolicySupplier =
                new PaperPolicySupplier<>(
                    clazz,
                    searchNodeMetadataFactory,
                    totalRiskAllowed,
                    random,
                    nodeSelector,
                    nnbasedEvaluator,
                    paperTreeUpdater,
                    treeUpdateConditionFactory);

            AbstractTrainer trainer = getAbstractTrainer(TrainerAlgorithm.EVERY_VISIT_MC,
                random,
                hallwayGameInitialInstanceSupplier,
                discountFactor,
                nnbasedEvaluator,
                paperTrainablePolicySupplier,
                replayBufferSize,
                stepCountLimit);


            long trainingStart = System.currentTimeMillis();
            for (int i = 0; i < stageCount; i++) {
                logger.info("Training policy for [{}]th iteration", i);
                trainer.trainPolicy(sampleEpisodeCount);
            }

            long trainingTimeInMs = System.currentTimeMillis() - trainingStart;

            logger.info("PaperPolicy test starts");

            String nnBasedPolicyName = "NNBased";


            PaperBenchmark<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> benchmark = new PaperBenchmark<>(
                Arrays.asList(new PaperBenchmarkingPolicy<>(nnBasedPolicyName, nnBasedPolicySupplier)),
                new EnvironmentPolicySupplier(random),
                hallwayGameInitialInstanceSupplier
            );

            long start = System.currentTimeMillis();
            List<PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl>> policyResultList = benchmark
                .runBenchmark(uniqueEpisodeCount, episodeCount, stepCountLimit);
            long end = System.currentTimeMillis();
            logger.info("Benchmarking took [{}] milliseconds", end - start);


            PaperPolicyResults<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> nnResults = policyResultList
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
        EnvironmentProbabilities,
        PaperMetadata<HallwayAction, DoubleReward>,
        HallwayStateImpl>
    getAbstractTrainer(TrainerAlgorithm trainerAlgorithm,
                       SplittableRandom random,
                       HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier,
                       double discountFactor,
                       PaperNodeEvaluatorWORKINGCOPY nodeEvaluator,
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


    public static HallwayGameInitialInstanceSupplier getHallwayGameInitialInstanceSupplier(SplittableRandom random, GameConfig gameConfig) throws NotValidGameStringRepresentationException, IOException {
        ClassLoader classLoader = PaperGenericsPrototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo0.txt");
//        URL url = classLoader.getResource("examples/hallway_demo2.txt");
//         URL url = classLoader.getResource("examples/hallway_demo3.txt");
//         URL url = classLoader.getResource("examples/hallway_demo4.txt");
//         URL url = classLoader.getResource("examples/hallway_demo5.txt");
//         URL url = classLoader.getResource("examples/hallway_demo6.txt");

//        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway1.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");
//        URL url = classLoader.getResource("examples/hallway0123.txt");
//        URL url = classLoader.getResource("examples/hallway0124.txt");
//        URL url = classLoader.getResource("examples/hallway0125s.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal2.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal3.txt");

//        URL url = classLoader.getResource("examples/trapsEverywhere.txt");


//        URL url = classLoader.getResource("examples/benchmark/benchmark_01.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_02.txt");
//        InputStream resourceAsStream = classLoader.getResourceAsStream("examples/benchmark/benchmark_03.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_04.txt");
        InputStream resourceAsStream = classLoader.getResourceAsStream("examples/benchmark/benchmark_05.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_06.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_07.txt");
//        InputStream resourceAsStream = classLoader.getResourceAsStream("examples/benchmark/benchmark_08.txt");

        byte[] bytes = resourceAsStream.readAllBytes();

        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(bytes));
    }

    private static void cleanUpNativeTempFiles() {
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
