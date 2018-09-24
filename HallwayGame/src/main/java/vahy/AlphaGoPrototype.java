package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.AlphaGoEpisodeAggregator;
import vahy.AlphaGo.reinforcement.AlphaGoTrainableApproximator;
import vahy.AlphaGo.reinforcement.learn.AlphaGoAbstractMonteCarloTrainer;
import vahy.AlphaGo.reinforcement.learn.AlphaGoFirstVisitMonteCarloTrainer;
import vahy.AlphaGo.reinforcement.learn.AlphaGoLinearNaiveModel;
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
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class AlphaGoPrototype {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoPrototype.class);

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {

        SplittableRandom random = new SplittableRandom(3);
        GameConfig gameConfig = new ConfigBuilder().reward(100).noisyMoveProbability(0.1).stepPenalty(1).trapProbability(1).buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = getHallwayGameInitialInstanceSupplier(random, gameConfig);

        double discountFactor = 0.999;
        double explorationConstant = 0.5;
        double learningRate = 0.000001;
        double cpuctParameter = 1;
        int treeUpdateCount = 200;
        int trainingEpochCount = 200;
        int sampleEpisodeCount = 10;

        int uniqueEpisodeCount = 1;
        int episodeCount = 10;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        AlphaGoTrainableApproximator trainableApproximator = new AlphaGoTrainableApproximator(new AlphaGoLinearNaiveModel(
            hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
            1 + ActionType.playerActions.length,
            learningRate
        ));

        AlphaGoTrainablePolicySupplier alphaGoTrainablePolicySupplier = new AlphaGoTrainablePolicySupplier(
            random,
            explorationConstant,
            discountFactor,
            trainableApproximator,
            cpuctParameter,
            treeUpdateCount
        );

        AlphaGoPolicySupplier alphaGoPolicySupplier = new AlphaGoPolicySupplier(
            cpuctParameter,
            treeUpdateCount,
            discountFactor,
            random,
            trainableApproximator
        );

        AlphaGoAbstractMonteCarloTrainer trainer = new AlphaGoFirstVisitMonteCarloTrainer(
            hallwayGameInitialInstanceSupplier,
            alphaGoTrainablePolicySupplier,
            new AlphaGoEnvironmentPolicySupplier(random),
            new DoubleScalarRewardAggregator(),
            discountFactor);

        for (int i = 0; i < trainingEpochCount; i++) {
            trainer.trainPolicy(sampleEpisodeCount);
        }

        logger.info("Policy test starts");

        AlphaGoEpisodeAggregator episodeAggregator = new AlphaGoEpisodeAggregator(
            uniqueEpisodeCount,
            episodeCount,
            hallwayGameInitialInstanceSupplier,
            alphaGoPolicySupplier,
            new AlphaGoEnvironmentPolicySupplier(random)
        );

        List<List<Double>> rewardHistory = episodeAggregator.runSimulation().stream().map(x -> x.stream().map(DoubleScalarReward::getValue).collect(Collectors.toList())).collect(Collectors.toList());
        // printChart(rewardHistory);
        logger.info("Total reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get());
        logger.info("Average reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get() / totalEpisodes);


    }

    public static HallwayGameInitialInstanceSupplier getHallwayGameInitialInstanceSupplier(SplittableRandom random, GameConfig gameConfig) throws NotValidGameStringRepresentationException, IOException {
        ClassLoader classLoader = AlphaGoPrototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo0.txt");
        URL url = classLoader.getResource("examples/hallway_demo2.txt");
//         URL url = classLoader.getResource("examples/hallway_demo3.txt");
//         URL url = classLoader.getResource("examples/hallway_demo4.txt");
//         URL url = classLoader.getResource("examples/hallway_demo5.txt");
//        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");

        File file = new File(url.getFile());
        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    }
}
