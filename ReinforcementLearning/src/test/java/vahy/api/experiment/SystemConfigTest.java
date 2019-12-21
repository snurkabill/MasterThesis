package vahy.api.experiment;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SystemConfigTest {

    @Test
    public void basicTest() {
        SystemConfig systemConfig = new SystemConfig(22, false, 11, false, 42, true);
        Assert.assertEquals(22, systemConfig.getRandomSeed());
        Assert.assertEquals(11, systemConfig.getParallelThreadsCount());
        Assert.assertEquals(42, systemConfig.getEvalEpisodeCount());
        Assert.assertFalse(systemConfig.isSingleThreadedEvaluation());
        Assert.assertFalse(systemConfig.isDrawWindow());
        Assert.assertTrue(systemConfig.dumpTrainingData());

        SystemConfig systemConfig2 = new SystemConfig(0, true, 1, true, 2, false);
        Assert.assertEquals(0, systemConfig2.getRandomSeed());
        Assert.assertEquals(1, systemConfig2.getParallelThreadsCount());
        Assert.assertEquals(2, systemConfig2.getEvalEpisodeCount());
        Assert.assertTrue(systemConfig2.isSingleThreadedEvaluation());
        Assert.assertTrue(systemConfig2.isDrawWindow());
        Assert.assertFalse(systemConfig2.dumpTrainingData());

        SystemConfig systemConfig3 = new SystemConfig(0, false, 1, true, 2, true);
        Assert.assertEquals(0, systemConfig3.getRandomSeed());
        Assert.assertEquals(1, systemConfig3.getParallelThreadsCount());
        Assert.assertEquals(2, systemConfig3.getEvalEpisodeCount());
        Assert.assertFalse(systemConfig3.isSingleThreadedEvaluation());
        Assert.assertTrue(systemConfig3.isDrawWindow());
        Assert.assertTrue(systemConfig3.dumpTrainingData());
    }

}
