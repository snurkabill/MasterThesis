package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeAggregator;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.chart.ChartBuilder;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.agent.policy.exhaustive.BfsPolicy;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.episode.EpisodeAggregatorImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.impl.search.simulation.MonteCarloSimulator;
import vahy.impl.search.update.UniformAverageDiscountEstimateRewardTransitionUpdater;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class Prototype {

    private static final Logger logger = LoggerFactory.getLogger(Prototype.class);

    public static void main(String[] args) throws IOException, NotValidGameStringRepresentationException {
        ClassLoader classLoader = Prototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo0.txt");
//        URL url = classLoader.getResource("examples/hallway_demo2.txt");
//         URL url = classLoader.getResource("examples/hallway_demo3.txt");
//         URL url = classLoader.getResource("examples/hallway_demo4.txt");
//         URL url = classLoader.getResource("examples/hallway_demo5.txt");
        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");

        File file = new File(url.getFile());
        SplittableRandom random = new SplittableRandom(2);
        GameConfig gameConfig = new ConfigBuilder().reward(1000).noisyMoveProbability(0.0).stepPenalty(1).trapProbability(1).buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));


        RewardAggregator<DoubleScalarReward> rewardAggregator = new DoubleScalarRewardAggregator();
        double discountFactor = 1;
        int uniqueEpisodeCount = 1;
        int episodeCount = 20;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

//        NodeTransitionUpdater<
//            ActionType,
//            DoubleScalarReward,
//            DoubleVectorialObservation,
//            Ucb1StateActionMetadata<DoubleScalarReward>,
//            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
//            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> transitionUpdater = new Ucb1WithGivenProbabilitiesTransitionUpdater(discountFactor, rewardAggregator);
//
//        NodeEvaluationSimulator<
//            ActionType,
//            DoubleScalarReward,
//            DoubleVectorialObservation,
//            Ucb1StateActionMetadata<DoubleScalarReward>,
//            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
//            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator = new MonteCarloSimulator<>(1000, discountFactor, random, rewardAggregator);

        NodeTransitionUpdater<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarReward>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> transitionUpdater = new UniformAverageDiscountEstimateRewardTransitionUpdater<>(discountFactor, rewardAggregator);

        NodeEvaluationSimulator<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            AbstractStateActionMetadata<DoubleScalarReward>,
            AbstractSearchNodeMetadata<ActionType, DoubleScalarReward, AbstractStateActionMetadata<DoubleScalarReward>>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator = new MonteCarloSimulator<>(1000, discountFactor, random, rewardAggregator);

        EpisodeAggregator<DoubleScalarReward> episodeAggregator = new EpisodeAggregatorImpl<>(
            uniqueEpisodeCount,
            episodeCount,
            hallwayGameInitialInstanceSupplier,

//            immutableState -> new ImmutableTuple<>(
//                new UniformRandomWalkPolicy(random),
//                immutableState),


            immutableState -> new ImmutableTuple<>(
                new BfsPolicy(
                    random,
                    200,
                    (ImmutableStateImpl) immutableState,
                    transitionUpdater,
                    rewardSimulator),
                immutableState),


//            immutableState -> new ImmutableTuple<>(
//                new EGreedyPolicy(
//                    random,
//                    1000,
//                    0.3,
//                    immutableState,
//                    transitionUpdater,
//                    rewardSimulator),
//                immutableState),

//            immutableState -> new ImmutableTuple<>(
//                new Ucb1Policy(
//                    random,
//                    2000,
//                    immutableState,
//                    transitionUpdater,
//                    rewardSimulator),
//                immutableState),

            new EnvironmentPolicy(random)
            );

        List<List<Double>> rewardHistory = episodeAggregator.runSimulation().stream().map(x -> x.stream().map(DoubleScalarReward::getValue).collect(Collectors.toList())).collect(Collectors.toList());
        printChart(rewardHistory);
        logger.info("Total reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get());
        logger.info("Average reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get() / totalEpisodes);
    }

    public static void printChart(List<List<Double>> rewardHistory) {
        LinkedList<Double> average = new LinkedList<>();
        for (int j = 0; j < rewardHistory.stream().mapToInt(List::size).max().getAsInt(); j++) {

            Double jThSum = 0.0;
            int count = 0;
            for (List<Double> aRewardHistory : rewardHistory) {
                if (aRewardHistory.size() > j) {
                    jThSum += aRewardHistory.get(j);
                    count++;
                }
            }
            average.add(jThSum / count);
        }

        LinkedList<Double> runningSum = new LinkedList<>();
        for (Double value : average) {
            if (runningSum.isEmpty()) {
                runningSum.add(value);
            } else {
                runningSum.add(runningSum.getLast() + value);
            }
        }
        List<List<Double>> datasets = new ArrayList<>();
        datasets.add(average);
        datasets.add(runningSum);
        ChartBuilder.chart(new File("average"), datasets, "datasets");
    }
}
