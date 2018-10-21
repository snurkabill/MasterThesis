package vahy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PolicySupplier;
import vahy.paper.policy.PaperTrainablePolicySupplier;
import vahy.paper.reinforcement.EpisodeAggregator;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.paper.reinforcement.learn.AbstractTrainer;
import vahy.paper.reinforcement.learn.EveryVisitMonteCarloTrainer;
import vahy.paper.reinforcement.learn.tf.TFModel;
import vahy.paper.tree.NodeEvaluator;
import vahy.environment.ActionType;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class PaperPrototype {

    private static final Logger logger = LoggerFactory.getLogger(PaperPrototype.class);

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        cleanUpNativeTempFiles();
        long seed = 0;
        SplittableRandom random = new SplittableRandom(seed);
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.5)
            .stepPenalty(2)
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


        // MCTS
        double cpuctParameter = 2;
        int treeUpdateCount = 500;

        // REINFORCEMENT
        double discountFactor = 0.999;
        double explorationConstant = 0.5;
        double temperature = 2;
        int sampleEpisodeCount = 20;
        int replayBufferSize = 100;
        int stageCountCount = 200;

        // NN
        int batchSize = 1;
        double learningRate = 0.001;
        int trainingEpochCount = 200;

        // risk optimization
        boolean optimizeFlowInSearchTree = true;
        double totalRiskAllowed = 0.0;

        // simmulation after training
        int uniqueEpisodeCount = 1;
        int episodeCount = 10000;
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

        PaperTrainablePolicySupplier paperTrainablePolicySupplier = new PaperTrainablePolicySupplier(
            random,
            explorationConstant,
            temperature,
            totalRiskAllowed,
            trainableApproximator,
            cpuctParameter,
            treeUpdateCount,
            optimizeFlowInSearchTree
        );

        PolicySupplier policySupplier = new PolicySupplier(
            cpuctParameter,
            treeUpdateCount,
            totalRiskAllowed,
            random,
            trainableApproximator,
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
            trainer.trainPolicy(sampleEpisodeCount);
        }

        logger.info("PaperPolicy test starts");

        EpisodeAggregator episodeAggregator = new EpisodeAggregator(
            uniqueEpisodeCount,
            episodeCount,
            hallwayGameInitialInstanceSupplier,
            policySupplier,
            new EnvironmentPolicySupplier(random)
        );

        List<List<Double>> rewardHistory = episodeAggregator.runSimulation().stream().map(x -> x.stream().map(DoubleScalarReward::getValue).collect(Collectors.toList())).collect(Collectors.toList());
        // printChart(rewardHistory);
        logger.info("Total reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get());
        logger.info("Average reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get() / totalEpisodes);

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

//        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway1.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");
//        URL url = classLoader.getResource("examples/hallway0123.txt");
//        URL url = classLoader.getResource("examples/hallway0124.txt");
        URL url = classLoader.getResource("examples/hallway0125s.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal2.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal3.txt");

        File file = new File(url.getFile());
        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    }

    private static void cleanUpNativeTempFiles() {
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
