package vahy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.ActionType;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.StateRepresentation;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.paper.benchmark.Benchmark;
import vahy.paper.benchmark.BenchmarkingPolicy;
import vahy.paper.benchmark.PolicyResults;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.TrainableApproximator;
import vahy.paper.reinforcement.learn.AbstractTrainer;
import vahy.paper.reinforcement.learn.EveryVisitMonteCarloTrainer;
import vahy.paper.reinforcement.learn.FirstVisitMonteCarloTrainer;
import vahy.paper.reinforcement.learn.ReplayBufferTrainer;
import vahy.paper.reinforcement.learn.Trainer;
import vahy.paper.reinforcement.learn.tf.TFModel;
import vahy.paper.tree.nodeEvaluator.ApproximatorBasedNodeEvaluator;
import vahy.paper.tree.nodeEvaluator.EmptyNodeEvaluator;
import vahy.paper.tree.nodeEvaluator.MCRolloutBasedNodeEvaluator;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.paper.tree.nodeExpander.McRolloutCompetitiveTreeExpander;
import vahy.paper.tree.nodeExpander.NodeExpander;
import vahy.paper.tree.nodeExpander.PaperNodeExpander;
import vahy.paper.tree.treeUpdater.PaperTreeUpdater;
import vahy.paper.tree.treeUpdater.PaperTreeUpdaterThatAlternatesSelectedFinalNodes;
import vahy.utils.EnumUtils;

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
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = getHallwayGameInitialInstanceSupplier(random, gameConfig);

        // TREE UPDATE POLICY
        // TreeUpdateCondition treeUpdateCondition = new TreeUpdateConditionSuplierCountBased(treeUpdateCount);
        // TreeUpdateCondition treeUpdateCondition = new TreeUpdateConditionImpl(5000, 10000, 100);
//         TreeUpdateConditionFactory treeUpdateConditionFactory = new HybridTreeUpdateConditionFactory(5_000, 50_000, 100);
        TreeUpdateConditionFactory treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(100);

        // MCTS
        double cpuctParameter = 2;

        // MCTS - mc rollout based
        int mcRolloutCount = 1;

        // REINFORCEMENT
        double discountFactor = 1;
        double explorationConstant = 0.3;
        double temperature = 2;
        int sampleEpisodeCount = 20;
        int replayBufferSize = 50;
        int stageCountCount = 300;

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


        // MCTS WITH NN EVAL
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

        ApproximatorBasedNodeEvaluator nnbasedEvaluator = new ApproximatorBasedNodeEvaluator(trainableApproximator);
        PaperNodeExpander paperNodeExpander = new PaperNodeExpander();
        PaperTreeUpdater paperTreeUpdater = new PaperTreeUpdater();
        PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier = new PaperTrainablePaperPolicySupplier(
            random,
            explorationConstant,
            temperature,
            totalRiskAllowed,
            nnbasedEvaluator,
            paperNodeExpander,
            paperTreeUpdater,
            treeUpdateConditionFactory,
            cpuctParameter,
            optimizeFlowInSearchTree
        );

        PaperPolicySupplier nnBasedPolicySupplier = new PaperPolicySupplier(
            cpuctParameter,
            totalRiskAllowed,
            random,
            nnbasedEvaluator,
            paperNodeExpander,
            paperTreeUpdater,
            treeUpdateConditionFactory,
            optimizeFlowInSearchTree);


        // MCTS WITH MC EVAL

        NodeExpander mcEvalNodeExpander = new PaperNodeExpander();
        PaperPolicySupplier mcBasedPolicySupplier = new PaperPolicySupplier(
            cpuctParameter,
            totalRiskAllowed,
            random,
            new MCRolloutBasedNodeEvaluator(random, mcRolloutCount, discountFactor),
            mcEvalNodeExpander,
            new PaperTreeUpdater(),
            treeUpdateConditionFactory,
            optimizeFlowInSearchTree);


        // MCTS WITH MC EXPAND + EVAL
        NodeExpander mcExpandEvalNodeExpander = new McRolloutCompetitiveTreeExpander(mcRolloutCount, random, discountFactor, new DoubleScalarRewardAggregator());
        PaperPolicySupplier mcBasedExpanderWithEvaluationPolicySupplier = new PaperPolicySupplier(
            cpuctParameter,
            totalRiskAllowed,
            random,
            new EmptyNodeEvaluator(),
            mcExpandEvalNodeExpander,
            new PaperTreeUpdaterThatAlternatesSelectedFinalNodes(),
            treeUpdateConditionFactory,
            optimizeFlowInSearchTree);

        AbstractTrainer trainer = getAbstractTrainer(Trainer.EVERY_VISIT_MC,
            random,
            hallwayGameInitialInstanceSupplier,
            discountFactor,
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
        String mcBasedPolicyName = "MCBased";
        String mcBasedExpandEvalPolicyName = "MCBasedExpandEval";


        Benchmark benchmark = new Benchmark(
            Arrays.asList(
                new BenchmarkingPolicy(nnBasedPolicyName, nnBasedPolicySupplier)
//                new BenchmarkingPolicy(mcBasedPolicyName, mcBasedPolicySupplier)
//                new BenchmarkingPolicy(mcBasedExpandEvalPolicyName, mcBasedExpanderWithEvaluationPolicySupplier)
            ),
            new EnvironmentPolicySupplier(random),
            hallwayGameInitialInstanceSupplier
            );

        long start = System.currentTimeMillis();
        List<PolicyResults> policyResultList = benchmark.runBenchmark(uniqueEpisodeCount, episodeCount, stepCountLimit);
        long end = System.currentTimeMillis();
        logger.info("Benchmarking took [{}] milliseconds", end - start);


        PolicyResults nnResults = policyResultList.stream().filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(nnBasedPolicyName)).findFirst().get();
//        PolicyResults mcResults = policyResultList.stream().filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(mcBasedPolicyName)).findFirst().get();
//        PolicyResults mcExpandEvalResults = policyResultList.stream().filter(x -> x.getBenchmarkingPolicy().getPolicyName().equals(mcBasedExpandEvalPolicyName)).findFirst().get();

        logger.info("NN Based Average reward: [{}]", nnResults.getAverageReward());
        logger.info("NN Based millis per episode: [{}]", nnResults.getAverageMillisPerEpisode());
        logger.info("NN Based total expanded nodes: [{}]", paperNodeExpander.getNodesExpandedCount());
        logger.info("NN Based kill ratio: [{}]", nnResults.getKillRatio());
        logger.info("NN Based kill counter: [{}]", nnResults.getAgentKillCounter());
        logger.info("NN Based training time: [{}]ms", trainingTimeInMs);

//        logger.info("MC Based Average reward: [{}]", mcResults.getAverageReward());
//        logger.info("MC Based millis per episode: [{}]", mcResults.getAverageMillisPerEpisode());
//        logger.info("MC Based total expanded nodes: [{}]", mcEvalNodeExpander.getNodesExpandedCount());
//        logger.info("MC Based kill ratio: [{}]", mcResults.getKillRatio());
//        logger.info("MC Based kill counter: [{}]", mcResults.getAgentKillCounter());

//        logger.info("MC Expand Eval Based Average reward: [{}]", mcExpandEvalResults.getAverageReward());
//        logger.info("MC Expand Eval Based millis per episode: [{}]", mcExpandEvalResults.getAverageMillisPerEpisode());
//        logger.info("MC Expand Eval Based total expanded nodes: [{}]", mcExpandEvalNodeExpander.getNodesExpandedCount());
//        logger.info("MC Expand Eval Based kill ratio: [{}]", mcExpandEvalResults.getKillRatio());
//        logger.info("MC Expand Eval Based kill counter: [{}]", mcExpandEvalResults.getAgentKillCounter());

        cleanUpNativeTempFiles();
    }

    private static AbstractTrainer getAbstractTrainer(Trainer trainer, SplittableRandom random, HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier, double discountFactor, PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier, int replayBufferSize, int stepCountLimit) {
        switch(trainer) {
            case REPLAY_BUFFER:
                return new ReplayBufferTrainer(
                    hallwayGameInitialInstanceSupplier,
                    paperTrainablePolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    replayBufferSize,
                    new DoubleScalarRewardAggregator(),
                    discountFactor, stepCountLimit);
            case FIRST_VISIT_MC:
                return new FirstVisitMonteCarloTrainer(
                    hallwayGameInitialInstanceSupplier,
                    paperTrainablePolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    new DoubleScalarRewardAggregator(),
                    discountFactor,
                    stepCountLimit);
            case EVERY_VISIT_MC:
                return new EveryVisitMonteCarloTrainer(
                    hallwayGameInitialInstanceSupplier,
                    paperTrainablePolicySupplier,
                    new EnvironmentPolicySupplier(random),
                    new DoubleScalarRewardAggregator(),
                    discountFactor,
                    stepCountLimit);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(trainer);

        }

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
//        URL url = classLoader.getResource("examples/hallway0125s.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal2.txt");
//        URL url = classLoader.getResource("examples/hallway-trap-minimal3.txt");

//        URL url = classLoader.getResource("examples/trapsEverywhere.txt");


//        URL url = classLoader.getResource("examples/benchmark/benchmark_01.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_02.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_03.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_04.txt");
        URL url = classLoader.getResource("examples/benchmark/benchmark_05.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_06.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_07.txt");
//        URL url = classLoader.getResource("examples/benchmark/benchmark_08.txt");

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


