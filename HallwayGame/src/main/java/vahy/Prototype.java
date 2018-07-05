package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeAggregator;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.chart.ChartBuilder;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.agent.policy.exhaustive.BfsPolicy;
import vahy.environment.agent.policy.randomized.EGreedyPolicy;
import vahy.environment.agent.policy.smart.Ucb1Policy;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.episode.EpisodeAggregatorImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.model.reward.DoubleScalarRewardFactory;
import vahy.impl.policy.random.UniformRandomWalkPolicy;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.impl.search.simulation.MonteCarloSimulator;
import vahy.impl.search.update.UniformAverageDiscountEstimateRewardTransitionUpdater;
import vahy.search.AbstractMetadataWithGivenProbabilitiesTransitionUpdater;
import vahy.search.Ucb1WithGivenProbabilitiesTransitionUpdater;

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
        SplittableRandom random = new SplittableRandom(2);
        GameConfig gameConfig = new ConfigBuilder().reward(1000).noisyMoveProbability(0.1).stepPenalty(1).trapProbability(1).buildConfig();
        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = getHallwayGameInitialInstanceSupplier(random, gameConfig);

        RewardAggregator<DoubleScalarRewardDouble> rewardAggregator = new DoubleScalarRewardAggregator();
        double discountFactor = 1;
        int uniqueEpisodeCount = 1;
        int episodeCount = 20;
        int monteCarloSimulationCount = 100;
        int updateTreeCount = 10000;
        int totalEpisodes = uniqueEpisodeCount * episodeCount;

        //            provideRandomWalkPolicy(random),
        //            provideBfsPolicy(random, rewardAggregator, discountFactor, monteCarloSimulationCount, updateTreeCount),
        //            provideEGreedyPolicy(random, rewardAggregator, discountFactor, monteCarloSimulationCount, updateTreeCount),
        //            provideUcb1Policy(random, rewardAggregator, discountFactor, monteCarloSimulationCount, updateTreeCount),
//        PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> policySupplier = provideEGreedyPolicy(random, rewardAggregator, discountFactor, monteCarloSimulationCount, updateTreeCount);

        PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> policySupplier = TrainedPolicyPrototype.trainPolicy(
            hallwayGameInitialInstanceSupplier,
            initialState -> new EnvironmentPolicy(random),
            new DoubleScalarRewardFactory(),
            rewardAggregator,
            discountFactor,
            hallwayGameInitialInstanceSupplier.createInitialState().getObservation().getObservedVector().length,
            1,
            0.1,
            random,
            updateTreeCount);

        EpisodeAggregator<DoubleScalarRewardDouble> episodeAggregator = new EpisodeAggregatorImpl<>(
            uniqueEpisodeCount,
            episodeCount,
            hallwayGameInitialInstanceSupplier,
            policySupplier,
            new EnvironmentPolicy(random)
            );

        List<List<Double>> rewardHistory = episodeAggregator.runSimulation().stream().map(x -> x.stream().map(DoubleScalarRewardDouble::getValue).collect(Collectors.toList())).collect(Collectors.toList());
        printChart(rewardHistory);
        logger.info("Total reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get());
        logger.info("Average reward: [{}]", rewardHistory.stream().map(x -> x.stream().reduce((aDouble, aDouble2) -> aDouble + aDouble2).get()).reduce((aDouble, aDouble2) -> aDouble + aDouble2).get() / totalEpisodes);
    }

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> provideRandomWalkPolicy(SplittableRandom random) {
        return immutableState -> new UniformRandomWalkPolicy<>(random);
    }

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> provideBfsPolicy(SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator, double discountFactor, int monteCarloSimulationCount, int uprateTreeCount) {
        return immutableState -> new BfsPolicy(
                random,
                uprateTreeCount,
                (ImmutableStateImpl) immutableState,
                provideNodeTransitionUpdaterForAbstractMetadata(discountFactor, rewardAggregator),
//                new CumulativeRewardSimulator<>()
                provideMcEstimatorForAbstractMetadata(monteCarloSimulationCount, discountFactor, random, rewardAggregator));
    }

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> provideUcb1Policy(SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator, double discountFactor, int monteCarloSimulationCount, int uprateTreeCount) {
        return immutableState -> new Ucb1Policy(
                random,
                uprateTreeCount,
                (ImmutableStateImpl) immutableState,
//                provideNodeTransitionUpdaterForUcb1Metadata(discountFactor, rewardAggregator),
                provideNodeTransitionUpdaterForUcb1WithGivenProbabilities(discountFactor, rewardAggregator),
                provideMcEstimatorForUcb1Metadata(monteCarloSimulationCount, discountFactor, random, rewardAggregator));
    }

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> provideEGreedyPolicy(SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator, double discountFactor, int monteCarloSimulationCount, int uprateTreeCount) {
        return immutableState -> new EGreedyPolicy(
                0.3,
                random,
                uprateTreeCount,
                (ImmutableStateImpl) immutableState,
//                provideNodeTransitionUpdaterForAbstractMetadata(discountFactor, rewardAggregator),
                provideNodeTransitionUpdaterForAbstractMetadataWithGivenProbabilities(discountFactor, rewardAggregator),
                provideMcEstimatorForAbstractMetadata(monteCarloSimulationCount, discountFactor, random, rewardAggregator));
    }

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> provideTrainedLinearModel(SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator, double discountFactor, int monteCarloSimulationCount, int uprateTreeCount) {

        NodeTransitionUpdater<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, AbstractStateActionMetadata<DoubleScalarRewardDouble>, AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>, State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeTransitionUpdater = provideNodeTransitionUpdaterForAbstractMetadataWithGivenProbabilities(discountFactor, rewardAggregator);
        NodeEvaluationSimulator<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation, AbstractStateActionMetadata<DoubleScalarRewardDouble>, AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>, State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> nodeEvaluationSimulator = provideMcEstimatorForAbstractMetadata(monteCarloSimulationCount, discountFactor, random, rewardAggregator);
        return immutableState -> new EGreedyPolicy(
            0.3,
            random,
            uprateTreeCount,
            (ImmutableStateImpl) immutableState,
            nodeTransitionUpdater,
            nodeEvaluationSimulator);
    }

    public static NodeTransitionUpdater<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        AbstractStateActionMetadata<DoubleScalarRewardDouble>,
        AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideNodeTransitionUpdaterForAbstractMetadata(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardRewardAggregator) {
        return new UniformAverageDiscountEstimateRewardTransitionUpdater<>(discountFactor, rewardRewardAggregator);
    }

    public static NodeTransitionUpdater<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideNodeTransitionUpdaterForUcb1Metadata(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardRewardAggregator) {
        return new UniformAverageDiscountEstimateRewardTransitionUpdater<>(discountFactor, rewardRewardAggregator);
    }

    public static NodeTransitionUpdater<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideNodeTransitionUpdaterForUcb1WithGivenProbabilities(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardRewardAggregator) {
        return new Ucb1WithGivenProbabilitiesTransitionUpdater(discountFactor, rewardRewardAggregator);
    }

    public static NodeTransitionUpdater<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        AbstractStateActionMetadata<DoubleScalarRewardDouble>,
        AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideNodeTransitionUpdaterForAbstractMetadataWithGivenProbabilities(double discountFactor, RewardAggregator<DoubleScalarRewardDouble> rewardRewardAggregator) {
        return new AbstractMetadataWithGivenProbabilitiesTransitionUpdater(discountFactor, rewardRewardAggregator);
    }

    public static NodeEvaluationSimulator<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        AbstractStateActionMetadata<DoubleScalarRewardDouble>,
        AbstractSearchNodeMetadata<ActionType, DoubleScalarRewardDouble, AbstractStateActionMetadata<DoubleScalarRewardDouble>>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideMcEstimatorForAbstractMetadata(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator) {
        return new MonteCarloSimulator<>(simulationCount, discountFactor, random, rewardAggregator);
    }

    public static NodeEvaluationSimulator<
        ActionType,
        DoubleScalarRewardDouble,
        DoubleVectorialObservation,
        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> provideMcEstimatorForUcb1Metadata(int simulationCount, double discountFactor, SplittableRandom random, RewardAggregator<DoubleScalarRewardDouble> rewardAggregator) {
        return new MonteCarloSimulator<>(simulationCount, discountFactor, random, rewardAggregator);
    }

    public static HallwayGameInitialInstanceSupplier getHallwayGameInitialInstanceSupplier(SplittableRandom random, GameConfig gameConfig) throws NotValidGameStringRepresentationException, IOException {
        ClassLoader classLoader = Prototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo0.txt");
//        URL url = classLoader.getResource("examples/hallway_demo2.txt");
//         URL url = classLoader.getResource("examples/hallway_demo3.txt");
//         URL url = classLoader.getResource("examples/hallway_demo4.txt");
         URL url = classLoader.getResource("examples/hallway_demo5.txt");
//        URL url = classLoader.getResource("examples/hallway0.txt");
//        URL url = classLoader.getResource("examples/hallway8.txt");
//        URL url = classLoader.getResource("examples/hallway1-traps.txt");

        File file = new File(url.getFile());
        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
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
