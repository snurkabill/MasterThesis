package vahy.api.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SystemConfigBuilderTest {

    @Test
    public void basicTest() {

        SystemConfigBuilder scb = new SystemConfigBuilder();
        var systemConfig = scb
            .setRandomSeed(0)
            .setDrawWindow(false)
            .setDumpTrainingData(true)
            .setEvalEpisodeCount(123)
            .setParallelThreadsCount(2)
            .setSingleThreadedEvaluation(true)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .buildSystemConfig();


        assertTrue(systemConfig.dumpTrainingData());
        assertFalse(systemConfig.isDrawWindow());
        assertTrue(systemConfig.isSingleThreadedEvaluation());
        assertEquals(systemConfig.getParallelThreadsCount(), 2);
        assertEquals(systemConfig.getEvalEpisodeCount(), 123);
        assertEquals(systemConfig.getRandomSeed(), 0);

        SystemConfigBuilder scb2 = new SystemConfigBuilder();
        var systemConfig2 = scb2
            .setRandomSeed(1)
            .setDrawWindow(true)
            .setDumpTrainingData(false)
            .setEvalEpisodeCount(256)
            .setParallelThreadsCount(42)
            .setSingleThreadedEvaluation(true)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .buildSystemConfig();

        assertFalse(systemConfig2.dumpTrainingData());
        assertTrue(systemConfig2.isDrawWindow());
        assertTrue(systemConfig2.isSingleThreadedEvaluation());
        assertEquals(systemConfig2.getParallelThreadsCount(), 42);
        assertEquals(systemConfig2.getEvalEpisodeCount(), 256);
        assertEquals(systemConfig2.getRandomSeed(), 1);
    }

    @Test
    public void reproducibilityTest() {
        SystemConfigBuilder scb = new SystemConfigBuilder();

        for (int i = 0; i < 10; i++) {
            var systemConfig = scb
                .setRandomSeed(i)
                .setDrawWindow(false)
                .setDumpTrainingData(true)
                .setEvalEpisodeCount(1)
                .setParallelThreadsCount(2)
                .setSingleThreadedEvaluation(true)
                .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
                .buildSystemConfig();

            assertTrue(systemConfig.dumpTrainingData());
            assertFalse(systemConfig.isDrawWindow());
            assertTrue(systemConfig.isSingleThreadedEvaluation());
            assertEquals(systemConfig.getParallelThreadsCount(), 2);
            assertEquals(systemConfig.getEvalEpisodeCount(), 1);
            assertEquals(systemConfig.getRandomSeed(), i);
        }
    }

    @Test
    public void randomnessTest() {
        SystemConfigBuilder scb = new SystemConfigBuilder();
        int failCounter = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            var systemConfig = scb
                .setRandomSeed(i)
                .setDrawWindow(false)
                .setDumpTrainingData(true)
                .setEvalEpisodeCount(1)
                .setParallelThreadsCount(2)
                .setSingleThreadedEvaluation(true)
                .setStochasticStrategy(StochasticStrategy.RANDOM)
                .buildSystemConfig();

            if(systemConfig.getRandomSeed() == i) {
                failCounter++;
            }
        }
        assertTrue(failCounter <= 10);
    }
}
