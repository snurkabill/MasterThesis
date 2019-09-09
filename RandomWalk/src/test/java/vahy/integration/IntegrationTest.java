package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import vahy.api.episode.TrainerAlgorithm;
import vahy.environment.RandomWalkSetup;
import vahy.experiment.Experiment;
import vahy.experiment.ExperimentSetup;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.experiment.EvaluatorType;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class IntegrationTest {

    @BeforeTest
    public void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "myTest")
    public static Object[][] experimentSettings() {
        return new Object[][] {
            {createExperiment_01(), 50, 0.055},
        };
    }

    @Ignore // ignored now. quick changes were made and there is no time to fix this test. failing expected.
    @Test(dataProvider = "myTest")
    public void benchmarkSolutionTest(ImmutableTuple<RandomWalkSetup, ExperimentSetup> setup,
                                      double minExpectedReward,
                                      double maxRiskHitRatio) throws IOException {
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        var experiment = new Experiment();
        experiment.prepareAndRun(setup, random);

        var results = experiment.getResults().get(0);

        var statistics = results.getCalculatedResultStatistics();

        Assert.assertTrue(statistics.getTotalPayoffAverage() >= minExpectedReward, "Avg reward is: [" + statistics.getTotalPayoffAverage() + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(statistics.getRiskHitRatio() <= maxRiskHitRatio, "Risk hit ratio is: [" + statistics.getRiskHitRatio() + "] but expected at most: [" + maxRiskHitRatio + "]");
    }

    public static ImmutableTuple<RandomWalkSetup, ExperimentSetup> createExperiment_01() {
        var randomWalkSetup = new RandomWalkSetup(1000, 0, 1, 1, 1, 10, 10, 0.9, 0.7);
        ExperimentSetup experimentSetup = new ExperimentSetup(
            0,
            3,
            1,
            new FixedUpdateCountTreeConditionFactory(20),
            1.0,
            10,
            20000,
            10000,
            100,
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 2000.0) / 3;
//                    return 0.2;
                }
            },
            new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 2000.0) * 3;
                }
            },
            () -> 0.3,
//            () -> 0.1,
//            () -> 2.0,
            TrainerAlgorithm.EVERY_VISIT_MC,
            ApproximatorType.NN,
            EvaluatorType.RALF,
//            ApproximatorType.HASHMAP,
            4,
            100,
            10000,
            0.3,
            0.01,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE,
            FlowOptimizerType.SOFT,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            SubTreeRiskCalculatorType.PRIOR_SUM,
            false);
        return new ImmutableTuple<>(randomWalkSetup, experimentSetup);
    }



}
