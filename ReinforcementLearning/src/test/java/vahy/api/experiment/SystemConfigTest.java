package vahy.api.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SystemConfigTest {

    @Test
    public void basicTest() {
        SystemConfig systemConfig = new SystemConfig(22, false, 11, false, 42, 0, false, true, false, null, null);
        assertEquals(22, systemConfig.getRandomSeed());
        assertEquals(11, systemConfig.getParallelThreadsCount());
        assertEquals(42, systemConfig.getEvalEpisodeCount());
        assertFalse(systemConfig.isSingleThreadedEvaluation());
        assertFalse(systemConfig.isDrawWindow());
        assertTrue(systemConfig.dumpTrainingData());

        SystemConfig systemConfig2 = new SystemConfig(0, true, 1, true, 2, 0, false, false, false, null, null);
        assertEquals(0, systemConfig2.getRandomSeed());
        assertEquals(1, systemConfig2.getParallelThreadsCount());
        assertEquals(2, systemConfig2.getEvalEpisodeCount());
        assertTrue(systemConfig2.isSingleThreadedEvaluation());
        assertTrue(systemConfig2.isDrawWindow());
        assertFalse(systemConfig2.dumpTrainingData());

        SystemConfig systemConfig3 = new SystemConfig(0, false, 1, true, 2, 0, false, true, false, null, null);

        assertEquals(0, systemConfig3.getRandomSeed());
        assertEquals(1, systemConfig3.getParallelThreadsCount());
        assertEquals(2, systemConfig3.getEvalEpisodeCount());
        assertFalse(systemConfig3.isSingleThreadedEvaluation());
        assertTrue(systemConfig3.isDrawWindow());
        assertTrue(systemConfig3.dumpTrainingData());
    }

}
