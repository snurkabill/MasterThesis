package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vahy.config.AlgorithmConfig;
import vahy.config.SystemConfig;
import vahy.environment.config.GameConfig;
import vahy.experiment.Experiment;
import vahy.game.HallwayInstance;
import vahy.paperGenerics.experiment.CalculatedResultStatistics;
import vahy.utils.ThirdPartBinaryUtils;

public abstract class AbstractHallwayTest {

    @BeforeTest
    public static void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "TestDataProviderMethod")
    public abstract Object[][] experimentSettings();

    @Test(dataProvider = "TestDataProviderMethod")
    public void benchmarkSolutionTest(AlgorithmConfig algorithmConfig, SystemConfig systemConfig, GameConfig gameConfig, HallwayInstance instance, double minExpectedReward, double maxRiskHitRatio) {
        var experiment = new Experiment(algorithmConfig, systemConfig);
        experiment.run(gameConfig, instance);
        var results = experiment.getResults().get(0);
        CalculatedResultStatistics stats = results.getCalculatedResultStatistics();
        Assert.assertTrue(stats.getTotalPayoffAverage() >= minExpectedReward, "Avg reward is: [" + stats.getTotalPayoffAverage() + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(stats.getRiskHitRatio() <= maxRiskHitRatio, "Risk hit ratio is: [" + stats.getRiskHitRatio() + "] but expected at most: [" + maxRiskHitRatio + "]");
    }
}
