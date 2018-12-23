package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.episode.EpisodeAggregator;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.environment.HallwayAction;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.agent.policy.player.smart.Ucb1Policy;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.episode.EpisodeAggregatorImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.MCTS.MonteCarloEvaluator;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateConditionSuplierCountBased;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class IntegrationTest {

    @Test
    public void MonteCarloSimpleTest() throws IOException, NotValidGameStringRepresentationException {
        ClassLoader classLoader = IntegrationTest.class.getClassLoader();
        URL url = classLoader.getResource("examples/hallway_demo3.txt");
        File file = new File(url.getFile());

        SplittableRandom random = new SplittableRandom(0);
        GameConfig gameConfig = new ConfigBuilder().reward(1000).noisyMoveProbability(0.0).stepPenalty(1).trapProbability(1).buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));

        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        double discountFactor = 0.9; // 0.9
        int rolloutCount = 20;


        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> rewardSimulator = new MonteCarloEvaluator<>(
            new SearchNodeBaseFactoryImpl<>(new MonteCarloTreeSearchMetadataFactory<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl>(rewardAggregator)),
            random,
            rewardAggregator,
            discountFactor,
            rolloutCount);

        EpisodeAggregator<DoubleReward> episodeAggregator = new EpisodeAggregatorImpl<>(
            1,
            10,
            hallwayGameInitialInstanceSupplier,
            initialState -> new Ucb1Policy(
                random,
                new TreeUpdateConditionSuplierCountBased(10000),
                5,
                initialState,
                rewardSimulator
                ),
            new EnvironmentPolicy(random)
        );
//            immutableState ->
//                new Ucb1Policy(
//                    random,
//                    new TreeUpdateConditionSuplierCountBased(10000),
//                    5,
//                    immutableState,
//                    rewardSimulator
//                    )

//                new EGreedyPolicy(
//                    random,
//                    new TreeUpdateConditionSuplierCountBased(100),
//                    immutableState,
//                    transitionUpdater,
//                    rewardSimulator
//                    ),


        List<List<Double>> rewardHistory = episodeAggregator.runSimulation().stream().map(x -> x.stream().map(DoubleReward::getValue).collect(Collectors.toList())).collect(Collectors.toList());
        System.out.println(rewardHistory.stream().mapToInt(List::size).average());
        Assert.assertTrue(rewardHistory.stream().allMatch(x -> x.size() == 17));
    }
}
