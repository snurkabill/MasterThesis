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
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.HallwayStateImpl;
import vahy.original.game.HallwayGameInitialInstanceSupplier;
import vahy.paperGenerics.PaperExperimentBuilder;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.utils.ThirdPartBinaryUtils;

import java.util.List;

public abstract class AbstractHallwayTest {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractHallwayTest.class.getName());

    public static final double TOLERANCE = Math.pow(10, -15);

    @BeforeTest
    public static void cleanUpNativeLibraries() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
    }

    @DataProvider(name = "TestDataProviderMethod")
    public abstract Object[][] experimentSettings();

    @Test(dataProvider = "TestDataProviderMethod")
    public void benchmarkSolutionTest(PaperAlgorithmConfig algorithmConfig, SystemConfig systemConfig, GameConfig gameConfig, double minExpectedReward, double maxRiskHitRatio) {

        var paperExperimentBuilder = new PaperExperimentBuilder<GameConfig, HallwayAction, HallwayStateImpl, HallwayStateImpl>()
            .setActionClass(HallwayAction.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(List.of(algorithmConfig))
            .setProblemConfig(gameConfig)
            .setProblemInstanceInitializerSupplier(HallwayGameInitialInstanceSupplier::new);

        var results = paperExperimentBuilder.execute();

        PaperEpisodeStatistics stats =  results.get(0).getEpisodeStatistics();

        double totalPayoffAverage = stats.getTotalPayoffAverage();
        double riskHitRatio = stats.getRiskHitRatio();

        logger.info("Avg reward: [{}], avg risk ratio: [{}]", totalPayoffAverage, riskHitRatio);
        logger.info("Expected avg reward: [{}], expected avg risk ratio: [{}]", minExpectedReward, maxRiskHitRatio);
        Assert.assertTrue(totalPayoffAverage >= minExpectedReward, "Avg reward is: [" + totalPayoffAverage + "] but expected at least: [" + minExpectedReward + "]");
        Assert.assertTrue(riskHitRatio <= maxRiskHitRatio, "Risk hit ratio is: [" + riskHitRatio + "] but expected at most: [" + maxRiskHitRatio + "]");
    }
}
