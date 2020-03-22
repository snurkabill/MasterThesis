package vahy.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vahy.api.experiment.SystemConfig;
import vahy.config.PaperAlgorithmConfig;
import vahy.original.environment.HallwayAction;
import vahy.original.environment.agent.policy.environment.HallwayPolicySupplier;
import vahy.original.environment.config.GameConfig;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.utils.ThirdPartBinaryUtils;

import java.nio.file.Path;

public abstract class AbstractHallwayTest {

;    protected static final Logger logger = LoggerFactory.getLogger(AbstractHallwayTest.class.getName());

    public static final double TOLERANCE = Math.pow(10, -15);

    @BeforeTest
    public static void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "TestDataProviderMethod")
    public abstract Object[][] experimentSettings();

    @Test(dataProvider = "TestDataProviderMethod")
    public void benchmarkSolutionTest(PaperAlgorithmConfig algorithmConfig, SystemConfig systemConfig, GameConfig gameConfig, double minExpectedReward, double maxRiskHitRatio) {

        var results = PaperExperimentEntryPoint.createExperimentAndRun(
            HallwayAction.class,
            HallwayGameInitialInstanceSupplier::new,
            HallwayPolicySupplier.class,
            algorithmConfig,
            systemConfig,
            gameConfig,
            Path.of("../Results")
        );

        PaperEpisodeStatistics stats = ((PaperEpisodeStatistics) results.get(0).getEpisodeStatistics());

        double totalPayoffAverage = stats.getTotalPayoffAverage();
        double riskHitRatio = stats.getRiskHitRatio();

        logger.info("Avg reward: [{}], avg risk ratio: [{}]", totalPayoffAverage, riskHitRatio);
        logger.info("Expected avg reward: [{}], expected avg risk ratio: [{}]", minExpectedReward, maxRiskHitRatio);
        Assert.assertTrue(totalPayoffAverage >= minExpectedReward, "Avg reward is: [" + totalPayoffAverage + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(riskHitRatio <= maxRiskHitRatio, "Risk hit ratio is: [" + riskHitRatio + "] but expected at most: [" + maxRiskHitRatio + "]");
    }
}
