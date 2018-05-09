package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.Prototype;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.agent.policy.randomized.EGreedyPolicy;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.episode.EpisodeAggregator;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.impl.search.simulation.MonteCarloSimulator;
import vahy.impl.search.update.UniformAverageDiscountEstimateRewardTransitionUpdater;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.SplittableRandom;

public class IntegrationTest {

    @Test
    public void MonteCarloSimpleTest() throws IOException, NotValidGameStringRepresentationException {
        ClassLoader classLoader = Prototype.class.getClassLoader();
        URL url = classLoader.getResource("examples/hallway_demo3.txt");
        File file = new File(url.getFile());

        SplittableRandom random = new SplittableRandom(0);
        GameConfig gameConfig = new ConfigBuilder().reward(1000).noisyMoveProbability(0.0).stepPenalty(1).trapProbability(1).buildConfig();
        InitialStateInstanceFactory initialStateInstanceFactory = new InitialStateInstanceFactory(gameConfig, random);

        RewardAggregator<DoubleScalarReward> rewardAggregator = new DoubleScalarRewardAggregator();
        double discountFactor = 0.9;

        NodeTransitionUpdater<
                    ActionType,
                    DoubleScalarReward,
                    EmptyStateActionMetadata<DoubleScalarReward>,
                    EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> transitionUpdater = new UniformAverageDiscountEstimateRewardTransitionUpdater<>(discountFactor, rewardAggregator);

        NodeEvaluationSimulator<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    EmptyStateActionMetadata<DoubleScalarReward>,
                    EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator = new MonteCarloSimulator<>(100, discountFactor, random, rewardAggregator);

        EpisodeAggregator episodeAggregator = new EpisodeAggregator(
            1,
            10,
            immutableState -> new ImmutableTuple<>(
                new EGreedyPolicy(
                    random,
                    100,
                    0.1,
                    immutableState,
                    transitionUpdater,
                    rewardSimulator),
                immutableState),
            new EnvironmentPolicy(random),
            initialStateInstanceFactory);
        List<List<Double>> rewardHistory = episodeAggregator.runSimulation(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
        Assert.assertTrue(rewardHistory.stream().allMatch(x -> x.size() == 17));
    }
}
