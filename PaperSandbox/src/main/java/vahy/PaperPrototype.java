package vahy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.ActionType;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paper.benchmark.Benchmark;
import vahy.paper.benchmark.BenchmarkingPolicy;
import vahy.paper.benchmark.PolicyResults;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePolicySupplier;
import vahy.paper.policy.PolicySupplier;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.paper.reinforcement.learn.AbstractTrainer;
import vahy.paper.reinforcement.learn.EveryVisitMonteCarloTrainer;
import vahy.paper.reinforcement.learn.tf.TFModel;
import vahy.paper.tree.nodeEvaluator.ApproximatorBasedNodeEvaluator;
import vahy.paper.tree.nodeEvaluator.MCRolloutBasedNodeEvaluator;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.paper.tree.treeUpdateConditionSupplier.TreeUpdateConditionSupplier;
import vahy.paper.tree.treeUpdateConditionSupplier.TreeUpdateConditionSupplierImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class PaperPrototype {

    private static final Logger logger = LoggerFactory.getLogger(PaperPrototype.class);

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        cleanUpNativeTempFiles();
        long seed = 0;
        SplittableRandom random = new SplittableRandom(seed);
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = getHallwayGameInitialInstanceSupplier(random, gameConfig);


//        double discountFactor = 1;
//        double explorationConstant = 0.5;
//        double temperature = 0.5;
//        double learningRate = 0.0001;
//        double cpuctParameter = 1;
//        int treeUpdateCount = 100;
//        int trainingEpochCount = 20;
//        int sampleEpisodeCount = 10;



//        double discountFactor = 1;
//        double explorationConstant = 0.5;
//        double temperature = 0.5;
//        double learningRate = 0.0001;
//        double cpuctParameter = 1;
//        int treeUpdateCount = 1000;
//        int trainingEpochCount = 20;
//        int sampleEpisodeCount = 10;


        // MCTS - nn based
        double cpuctParameter = 2;
        int treeUpdateCount = 100;

        // MCTS - mc rollout based
        int mcRolloutCount = 10;

        // REINFORCEMENT
        double discountFactor = 0.999;
        double explorationConstant = 0.3;
        double temperature = 2;
        int sampleEpisodeCount = 10;
        int replayBufferSize = 50;
        int stageCountCount = 5;

        // NN
        int batchSize = 8;
        double learningRate = 0.001;
        int trainingEpochCount = 100;

        // risk optimization
        boolean optimizeFlowInSearchTree = true;
        double totalRiskAllowed = 0.10;

        // simmulation after training
        int uniqueEpisodeCount = 1;
        int episodeCount = 100;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        TrainableApproximator trainableApproximator = new TrainableApproximator(
//            new AlphaGoLinearNaiveModel(
//                hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
//                1 + ActionType.playerActions.length,
//                learningRate
//            )

//            new Dl4jModel(
//                hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
//                NodeEvaluator.POLICY_START_INDEX + ActionType.playerActions.length,
//                null,
//                seed,
//                learningRate,
//                trainingEpochCount
//            )



            new TFModel(
                hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
                NodeEvaluator.POLICY_START_INDEX + ActionType.playerActions.length,
                trainingEpochCount,
                batchSize,
                new File(TestingDL4J.class.getClassLoader().getResource("tfModel/graph.pb").getFile()),
                random
            )
        );


//        TreeUpdateConditionSupplier treeUpdateConditionSupplier = new TreeUpdateConditionSuplierCountBased(treeUpdateCount);
        TreeUpdateConditionSupplier treeUpdateConditionSupplier = new TreeUpdateConditionSupplierImpl(5000, 10000, 100);

        ApproximatorBasedNodeEvaluator nnbasedEvaluator = new ApproximatorBasedNodeEvaluator(trainableApproximator);
        NodeEvaluator mcBasedEvaluator = new MCRolloutBasedNodeEvaluator(random, mcRolloutCount, discountFactor);

        PaperTrainablePolicySupplier paperTrainablePolicySupplier = new PaperTrainablePolicySupplier(
            random,
            explorationConstant,
            temperature,
            totalRiskAllowed,
            nnbasedEvaluator,
            treeUpdateConditionSupplier,
            cpuctParameter,
            optimizeFlowInSearchTree
        );

        PolicySupplier nnBasedPolicySupplier = new PolicySupplier(
            cpuctParameter,
            totalRiskAllowed,
            random,
            nnbasedEvaluator,
            treeUpdateConditionSupplier,
            optimizeFlowInSearchTree);

        PolicySupplier mcBasedPolicySupplier = new PolicySupplier(
            cpuctParameter,
            totalRiskAllowed,
            random,
            mcBasedEvaluator,
            treeUpdateConditionSupplier,
            optimizeFlowInSearchTree);


//        AbstractTrainer trainer = new FirstVisitMonteCarloTrainer(
//            hallwayGameInitialInstanceSupplier,
//            paperTrainablePolicySupplier,
//            new EnvironmentPolicySupplier(random),
//            new DoubleScalarRewardAggregator(),
//            discountFactor);


        AbstractTrainer trainer = new EveryVisitMonteCarloTrainer(
            hallwayGameInitialInstanceSupplier,
            paperTrainablePolicySupplier,
            new EnvironmentPolicySupplier(random),
            new DoubleScalarRewardAggregator(),
            discountFactor);

//        AbstractTrainer trainer = new ReplayBufferTrainer(
//            hallwayGameInitialInstanceSupplier,
//            paperTrainablePolicySupplier,
//            new EnvironmentPolicySupplier(random),
//            replayBufferSize,
//            new DoubleScalarRewardAggregator(),
//            discountFactor);


        for (int i = 0; i < stageCountCount; i++) {
            logger.info("Training policy for [{}]th iteration", i);
            trainer.trainPolicy(sampleEpisodeCount);
        }

        logger.info("PaperPolicy test starts");

        String nnBasedPolicyName = "NNBased";
        String mcBasedPolicyName = "MCBased";


        Benchmark benchmark = new Benchmark(
            Arrays.asList(new BenchmarkingPolicy(nnBasedPolicyName, nnBasedPolicySupplier), new BenchmarkingPolicy(mcBasedPolicyName, mcBasedPolicySupplier)),
            new EnvironmentPolicySupplier(random),
            hallwayGameInitialInstanceSupplier
            );

        long start = System.currentTimeMillis();
        List<PolicyResults> policyResultList = benchmark.runBenchmark(uniqueEpisodeCount, episodeCount);
        long end = System.currentTimeMillis();
        logger.info("Benchmarking took [{}] milliseconds", end - start);

        PolicyResults nnResults = policyResultList.stream().filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName)).findFirst().get();
        PolicyResults mcResults = policyResultList.stream().filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(mcBasedPolicyName)).findFirst().get();

        logger.info("NN Based Average reward: [{}]", nnResults.getAverageReward());
        logger.info("NN Based millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
        logger.info("NN Based kill ratio: [{}]", nnResults.getKillRatio());
        logger.info("NN Based kill counter: [{}]", nnResults.getAgentKillCounter());

        logger.info("MC Based Average reward: [{}]", mcResults.getAverageReward());
        logger.info("MC Based millis per episode: [{}]", mcResults.getAverageMillisPerEpisode());
        logger.info("MC Based kill ratio: [{}]", mcResults.getKillRatio());
        logger.info("MC Based kill counter: [{}]", mcResults.getAgentKillCounter());

        cleanUpNativeTempFiles();
    }


    public static HallwayGameInitialInstanceSupplier getHallwayGameInitialInstanceSupplier(SplittableRandom random, GameConfig gameConfig) throws NotValidGameStringRepresentationException, IOException {
        ClassLoader classLoader = PaperPrototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo0.txt");
//        URL url = classLoader.getResource("examples/hallway_demo2.txt");
//         URL url = classLoader.getResource("examples/hallway_demo3.txt");
//         URL url = classLoader.getResource("examples/hallway_demo4.txt");
//         URL url = classLoader.getResource("examples/hallway_demo5.txt");
//         URL url = classLoader.getResource("examples/hallway_demo6.txt");

        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway1.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");
//        URL url = classLoader.getResource("examples/hallway0123.txt");
//        URL url = classLoader.getResource("examples/hallway0124.txt");
//        URL url = classLoader.getResource("examples/hallway0125s.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal2.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal3.txt");

        File file = new File(url.getFile());
        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
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
