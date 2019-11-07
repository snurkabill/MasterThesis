package vahy.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vahy.config.AlgorithmConfigImpl;
import vahy.api.experiment.SystemConfig;
import vahy.environment.config.GameConfig;
import vahy.experiment.Experiment;
import vahy.game.HallwayInstance;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.utils.ThirdPartBinaryUtils;

public abstract class AbstractHallwayTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHallwayTest.class.getName());

    public static final double TOLERANCE = Math.pow(10, -15);

    @BeforeTest
    public static void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "TestDataProviderMethod")
    public abstract Object[][] experimentSettings();

    @Test(dataProvider = "TestDataProviderMethod")
    public void benchmarkSolutionTest(AlgorithmConfigImpl algorithmConfig, SystemConfig systemConfig, GameConfig gameConfig, HallwayInstance instance, double minExpectedReward, double maxRiskHitRatio) {
        var experiment = new Experiment(algorithmConfig, systemConfig);
        experiment.run(gameConfig, instance);
        var results = experiment.getResults().get(0);
        PaperEpisodeStatistics stats = ((PaperEpisodeStatistics) results.getEpisodeStatistics());

        double totalPayoffAverage = stats.getTotalPayoffAverage();
        double riskHitRatio = stats.getRiskHitRatio();

        logger.info("Avg reward: [{}], avg risk ratio: [{}]", totalPayoffAverage, riskHitRatio);
        Assert.assertTrue(totalPayoffAverage >= minExpectedReward, "Avg reward is: [" + totalPayoffAverage + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(riskHitRatio <= maxRiskHitRatio, "Risk hit ratio is: [" + riskHitRatio + "] but expected at most: [" + maxRiskHitRatio + "]");
    }
}
