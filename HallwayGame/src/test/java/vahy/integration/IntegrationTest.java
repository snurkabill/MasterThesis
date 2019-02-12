package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vahy.api.episode.TrainerAlgorithm;
import vahy.data.HallwayInstance;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.StateRepresentation;
import vahy.experiment.Experiment;
import vahy.experiment.ExperimentSetup;
import vahy.experiment.ExperimentSetupBuilder;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.riskBasedSearch.SelectorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;

public class IntegrationTest {

    @BeforeTest
    public void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "myTest")
    public static Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_03(), 50, 0.055},
            {createExperiment_05(), 280, 0.0},
            {createExperiment_07(), 50, 0.0},
        };
    }

    @Test(dataProvider = "myTest")
    public void benchmarkSolutionTest(ImmutableTuple<GameConfig, ExperimentSetup> setup,
                                      double minExpectedReward,
                                      double maxRiskHitRatio) throws NotValidGameStringRepresentationException, IOException {
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        var experiment = new Experiment();
        experiment.prepareAndRun(setup, random);

        var results = experiment.getResults().get(0);

        Assert.assertTrue(results.getAverageReward() >= minExpectedReward, "Avg reward is: [" + results.getAverageReward() + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(results.getRiskHitRatio() <= maxRiskHitRatio, "Risk hit ratio is: [" + results.getRiskHitRatio() + "] but expected at most: [" + maxRiskHitRatio + "]");
    }

    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment_03() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(10)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_03)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .stageCount(100)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(10000)
            .globalRiskAllowed(0.05)
            .explorationConstantSupplier(() -> 0.2)
            .temperatureSupplier(() -> 2.0)
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }

    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment_05() {

        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_05)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)
            .trainingEpochCount(10)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .stageCount(100)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.00)
            .explorationConstantSupplier(() -> 0.2)
            .temperatureSupplier(() -> 1.5)
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }

    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment_07() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.4)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_07)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .stageCount(100)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.00)
            .explorationConstantSupplier(() -> 0.0)
            .temperatureSupplier(() -> 0.0)
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }
}
